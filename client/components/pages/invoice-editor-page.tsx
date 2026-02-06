"use client"

import { useEffect, useMemo, useRef, useState } from "react"
import { useNavigate, useParams, useSearchParams } from "react-router-dom"
import { ArrowLeft, Save, Send, Eye } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { ClientPicker } from "@/components/ui/client-picker"
import { LineItemsEditor } from "@/components/ui/line-items-editor"
import { services } from "@/lib/api-shim"
import type {
  CustomerDetailResponse,
  InvoiceDetailResponse,
  InvoiceItemResponse,
  QuoteDetailResponse,
  QuoteItemResponse,
} from "@/lib/api-shim"
import type { LineItem } from "@/lib/types"

const createEmptyLineItem = (): LineItem => ({
  id: `item_${Math.random().toString(36).slice(2, 11)}`,
  description: "",
  quantity: 1,
  unitPrice: 0,
  vatRate: 22,
  discount: 0,
  total: 0,
})

const toDateInput = (value?: string | null) => (value ? value.split("T")[0] : "")

const mapInvoiceItemsToLineItems = (items: InvoiceItemResponse[]): LineItem[] =>
  items.map((item) => {
    const quantity = item.quantity ?? 1
    const unitPrice = item.unitPrice ?? 0
    const gross = quantity * unitPrice
    const discountType = item.discountType ?? "NONE"
    const discountValue = item.discountValue ?? 0
    const discount =
      discountType === "PERCENT"
        ? discountValue
        : discountType === "AMOUNT" && gross > 0
          ? (discountValue / gross) * 100
          : 0

    return {
      id: String(item.id),
      description: item.description ?? "",
      quantity,
      unit: item.unit,
      unitPrice,
      vatRate: item.taxRate ?? 0,
      discount,
      notes: item.notes,
      total: item.lineTotalAmount ?? gross,
    }
  })

const mapQuoteItemsToLineItems = (items: QuoteItemResponse[]): LineItem[] =>
  items.map((item) => {
    const quantity = item.quantity ?? 1
    const unitPrice = item.unitPrice ?? 0
    const gross = quantity * unitPrice
    const discountType = item.discountType ?? "NONE"
    const discountValue = item.discountValue ?? 0
    const discount =
      discountType === "PERCENT"
        ? discountValue
        : discountType === "AMOUNT" && gross > 0
          ? (discountValue / gross) * 100
          : 0

    return {
      id: `quote_${item.id}`,
      description: item.description ?? "",
      quantity,
      unit: item.unit,
      unitPrice,
      vatRate: item.taxRate ?? 0,
      discount,
      notes: item.notes,
      total: item.lineTotalAmount ?? gross,
    }
  })

const formatInvoiceNumber = (year: number | null | undefined, number: number | null | undefined, id: string) => {
  if (year && number !== null && number !== undefined) {
    return `INV-${year}-${String(number).padStart(3, "0")}`
  }
  return `INV-${id}`
}

export function InvoiceEditorPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [searchParams] = useSearchParams()

  const quoteId = searchParams.get("quoteId") || searchParams.get("fromQuote")
  const preselectedClientId = searchParams.get("clientId")
  const isEditing = Boolean(id)

  const [selectedClientId, setSelectedClientId] = useState<string | null>(preselectedClientId)
  const [customer, setCustomer] = useState<CustomerDetailResponse | null>(null)

  const [invoiceNumber, setInvoiceNumber] = useState("")
  const [issueDate, setIssueDate] = useState(new Date().toISOString().split("T")[0])
  const [dueDate, setDueDate] = useState(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split("T")[0])
  const [lineItems, setLineItems] = useState<LineItem[]>([createEmptyLineItem()])
  const [notes, setNotes] = useState("")
  const [isSaving, setIsSaving] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  const originalInvoiceItemIds = useRef<Set<string>>(new Set())
  const currentInvoiceId = useRef<string | null>(null)
  const sourceQuoteIdRef = useRef<string | null>(quoteId)

  const subtotal = lineItems.reduce((sum, item) => sum + item.quantity * item.unitPrice * (1 - (item.discount ?? 0) / 100), 0)
  const taxAmount = lineItems.reduce((sum, item) => {
    const lineNet = item.quantity * item.unitPrice * (1 - (item.discount ?? 0) / 100)
    return sum + lineNet * ((item.vatRate ?? 0) / 100)
  }, 0)
  const total = subtotal + taxAmount

  const selectedClientName = useMemo(
    () => customer?.displayName ?? customer?.legalName ?? "Customer",
    [customer]
  )

  useEffect(() => {
    let active = true

    const load = async () => {
      setIsLoading(true)
      try {
        if (isEditing && id) {
          const detail: InvoiceDetailResponse = await services.invoices.getInvoice(id)
          if (!active) return

          currentInvoiceId.current = String(detail.id)
          sourceQuoteIdRef.current = detail.sourceQuoteId ? String(detail.sourceQuoteId) : sourceQuoteIdRef.current
          setSelectedClientId(detail.customerId ? String(detail.customerId) : null)
          setInvoiceNumber(formatInvoiceNumber(detail.invoiceYear, detail.invoiceNumber, String(detail.id)))
          setIssueDate(toDateInput(detail.issueDate) || new Date().toISOString().split("T")[0])
          setDueDate(toDateInput(detail.dueDate) || new Date().toISOString().split("T")[0])
          setNotes(detail.notes ?? "")

          const items = await services.invoiceItems.listItems(id)
          if (!active) return
          originalInvoiceItemIds.current = new Set(items.map((item) => String(item.id)))
          const mapped = mapInvoiceItemsToLineItems(items)
          setLineItems(mapped.length > 0 ? mapped : [createEmptyLineItem()])
          return
        }

        if (quoteId) {
          const quoteDetail: QuoteDetailResponse = await services.quotes.getQuote(quoteId)
          if (!active) return

          sourceQuoteIdRef.current = quoteId
          setSelectedClientId(quoteDetail.customerId ? String(quoteDetail.customerId) : preselectedClientId)
          setIssueDate(new Date().toISOString().split("T")[0])
          setDueDate(toDateInput(quoteDetail.validUntil) || new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split("T")[0])
          setNotes(quoteDetail.notes ?? "")

          const quoteItems = await services.quoteItems.listItems(quoteId)
          if (!active) return
          const mappedQuoteItems = mapQuoteItemsToLineItems(quoteItems)
          setLineItems(mappedQuoteItems.length > 0 ? mappedQuoteItems : [createEmptyLineItem()])
          setInvoiceNumber("(assigned by backend)")
          return
        }

        setInvoiceNumber("(assigned by backend)")
      } finally {
        if (active) setIsLoading(false)
      }
    }

    load()
    return () => {
      active = false
    }
  }, [id, isEditing, preselectedClientId, quoteId])

  useEffect(() => {
    let active = true
    if (!selectedClientId) {
      setCustomer(null)
      return
    }

    services.customers
      .getCustomer(selectedClientId)
      .then((data) => {
        if (active) setCustomer(data)
      })
      .catch(() => {
        if (active) setCustomer(null)
      })

    return () => {
      active = false
    }
  }, [selectedClientId])

  const toItemPayload = (item: LineItem, position: number) => ({
    position,
    description: item.description,
    notes: item.notes,
    quantity: item.quantity,
    unit: item.unit,
    unitPrice: item.unitPrice,
    taxRate: item.vatRate,
    discountType: ((item.discount ?? 0) > 0 ? "PERCENT" : "NONE") as "PERCENT" | "NONE",
    discountValue: item.discount ?? 0,
  })

  const syncInvoiceItems = async (invoiceId: string) => {
    const currentIds = new Set(lineItems.map((i) => i.id))
    const removed = Array.from(originalInvoiceItemIds.current).filter((savedId) => !currentIds.has(savedId))

    await Promise.all(removed.map((itemId) => services.invoiceItems.deleteItem(invoiceId, itemId)))

    await Promise.all(
      lineItems.map((item, index) => {
        const payload = toItemPayload(item, index + 1)
        if (/^\d+$/.test(item.id)) {
          return services.invoiceItems.updateItem(invoiceId, item.id, payload)
        }
        return services.invoiceItems.addItem(invoiceId, payload)
      })
    )
  }

  const handleSave = async (issueAfterSave: boolean) => {
    if (!selectedClientId) {
      window.alert("Please select a client")
      return
    }

    if (lineItems.length === 0 || lineItems.every((i) => !i.description.trim())) {
      window.alert("Please add at least one line item")
      return
    }

    setIsSaving(true)
    try {
      let invoiceId = currentInvoiceId.current

      if (!invoiceId) {
        const created = await services.invoices.createInvoice({
          customerId: Number(selectedClientId),
          sourceQuoteId: sourceQuoteIdRef.current ? Number(sourceQuoteIdRef.current) : undefined,
          notes,
          issueDate,
          dueDate,
          items: lineItems.map((item, index) => toItemPayload(item, index + 1)),
        })
        invoiceId = String(created.id)
        currentInvoiceId.current = invoiceId
        originalInvoiceItemIds.current = new Set()
      } else {
        await services.invoices.updateInvoice(invoiceId, {
          notes,
          issueDate,
          dueDate,
        })
        await syncInvoiceItems(invoiceId)
      }

      if (issueAfterSave && invoiceId) {
        await services.invoices.issueInvoice(invoiceId)
      }

      navigate(`/invoices/${invoiceId}`)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="m-4 space-y-6 overflow-x-hidden">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-2xl font-semibold text-foreground">
              {isEditing ? "Edit Invoice" : "New Invoice"}
            </h1>
            <p className="text-sm text-muted-foreground">
              {isLoading ? "Loading..." : isEditing ? `Editing ${invoiceNumber}` : "Create a new invoice"}
            </p>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={() => navigate(`/invoices/preview?draft=true`)}>
            <Eye className="mr-2 h-4 w-4" />
            Preview
          </Button>
          <Button variant="outline" onClick={() => handleSave(false)} disabled={isSaving || isLoading}>
            <Save className="mr-2 h-4 w-4" />
            Save Draft
          </Button>
          <Button onClick={() => handleSave(true)} disabled={isSaving || isLoading}>
            <Send className="mr-2 h-4 w-4" />
            Issue Invoice
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Invoice Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 sm:grid-cols-3">
                <div className="space-y-2">
                  <Label htmlFor="invoiceNumber">Invoice Number</Label>
                  <Input id="invoiceNumber" value={invoiceNumber} disabled />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="issueDate">Issue Date</Label>
                  <Input
                    id="issueDate"
                    type="date"
                    value={issueDate}
                    onChange={(e) => setIssueDate(e.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="dueDate">Due Date</Label>
                  <Input
                    id="dueDate"
                    type="date"
                    value={dueDate}
                    onChange={(e) => setDueDate(e.target.value)}
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Client</CardTitle>
            </CardHeader>
            <CardContent>
              <ClientPicker value={selectedClientId} onChange={setSelectedClientId} />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Line Items</CardTitle>
            </CardHeader>
            <CardContent className="overflow-x-auto">
              <LineItemsEditor items={lineItems} onChange={setLineItems} />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Additional Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="notes">Notes</Label>
                <Textarea
                  id="notes"
                  placeholder="Notes visible to client..."
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  rows={3}
                />
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card className="sticky top-6">
            <CardHeader>
              <CardTitle>Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Subtotal</span>
                <span className="font-medium">${subtotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Tax</span>
                <span className="font-medium">${taxAmount.toFixed(2)}</span>
              </div>
              <div className="border-t pt-4">
                <div className="flex justify-between">
                  <span className="text-lg font-semibold">Total</span>
                  <span className="text-lg font-bold text-primary">${total.toFixed(2)}</span>
                </div>
              </div>

              <div className="rounded-lg border bg-muted/50 p-3 text-sm">
                <p className="font-medium">{selectedClientName}</p>
                <p className="text-muted-foreground">{customer?.email ?? "-"}</p>
                <p className="text-muted-foreground">{customer?.phone ?? "-"}</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
