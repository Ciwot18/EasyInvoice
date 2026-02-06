"use client"

import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import {
  ArrowLeft,
  Edit,
  Send,
  Download,
  MoreHorizontal,
  CheckCircle,
  Clock,
  FileText,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { StatusBadge } from "@/components/ui/status-badge"
import { formatCurrency, formatDate } from "@/lib/utils"
import { services } from "@/lib/api-shim"
import type { CustomerDetailResponse, InvoiceDetailResponse, InvoiceItemResponse } from "@/lib/api-shim"
import type { InvoiceStatus } from "@/lib/types"

type InvoiceView = {
  id: string
  number: string
  status: InvoiceStatus
  title?: string | null
  notes?: string | null
  issueDate?: string | null
  dueDate?: string | null
  subtotalAmount?: number | null
  taxAmount?: number | null
  totalAmount?: number | null
  customerId?: string | null
  customerDisplayName?: string | null
  sourceQuoteId?: string | null
  createdAt?: string | null
  updatedAt?: string | null
  currency?: string | null
}

const mapInvoiceStatus = (status: InvoiceDetailResponse["status"]): InvoiceStatus => {
  switch (status) {
    case "DRAFT":
      return "draft"
    case "ISSUED":
      return "issued"
    case "PAID":
      return "paid"
    case "OVERDUE":
      return "overdue"
    case "ARCHIVED":
      return "archived"
    default:
      return "draft"
  }
}

const formatInvoiceNumber = (year: number | null, number: number | null, id: string) => {
  if (year && number !== null && number !== undefined) {
    return `INV-${year}-${String(number).padStart(3, "0")}`
  }
  return `INV-${id}`
}

const mapInvoiceView = (invoice: InvoiceDetailResponse): InvoiceView => ({
  id: String(invoice.id),
  number: formatInvoiceNumber(invoice.invoiceYear ?? null, invoice.invoiceNumber ?? null, String(invoice.id)),
  status: mapInvoiceStatus(invoice.status),
  title: invoice.title ?? null,
  notes: invoice.notes ?? null,
  issueDate: invoice.issueDate ?? null,
  dueDate: invoice.dueDate ?? null,
  subtotalAmount: invoice.subtotalAmount ?? null,
  taxAmount: invoice.taxAmount ?? null,
  totalAmount: invoice.totalAmount ?? null,
  customerId: invoice.customerId ? String(invoice.customerId) : null,
  customerDisplayName: invoice.customerDisplayName ?? null,
  sourceQuoteId: invoice.sourceQuoteId ? String(invoice.sourceQuoteId) : null,
  createdAt: invoice.createdAt ?? null,
  updatedAt: invoice.updatedAt ?? null,
  currency: invoice.currency ?? null,
})

export function InvoiceDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams()

  const [invoice, setInvoice] = useState<InvoiceView | null>(null)
  const [items, setItems] = useState<InvoiceItemResponse[]>([])
  const [customer, setCustomer] = useState<CustomerDetailResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    if (!id) return
    setIsLoading(true)
    setError(null)

    const load = async () => {
      try {
        const detail = await services.invoices.getInvoice(id)
        const itemList = await services.invoiceItems.listItems(id)
        if (!active) return
        const mapped = mapInvoiceView(detail)
        setInvoice(mapped)
        setItems(itemList)
        if (mapped.customerId) {
          const customerDetail = await services.customers.getCustomer(mapped.customerId)
          if (active) setCustomer(customerDetail)
        } else {
          setCustomer(null)
        }
      } catch (err) {
        if (!active) return
        setError(err instanceof Error ? err.message : "Failed to load invoice.")
      } finally {
        if (active) setIsLoading(false)
      }
    }

    load()
    return () => {
      active = false
    }
  }, [id])

  if (!invoice) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <p className="text-lg text-muted-foreground">
          {error ?? (isLoading ? "Loading invoice..." : "Invoice not found")}
        </p>
        <Button variant="link" onClick={() => navigate("/invoices")}>
          Back to Invoices
        </Button>
      </div>
    )
  }

  const handleMarkAsPaid = async () => {
    if (!id) return
    await services.invoices.payInvoice(id)
    const updated = await services.invoices.getInvoice(id)
    setInvoice(mapInvoiceView(updated))
  }

  const handleMarkOverdue = async () => {
    if (!id) return
    await services.invoices.markOverdue(id)
    const updated = await services.invoices.getInvoice(id)
    setInvoice(mapInvoiceView(updated))
  }

  const handleIssue = async () => {
    if (!id) return
    await services.invoices.issueInvoice(id)
    const updated = await services.invoices.getInvoice(id)
    setInvoice(mapInvoiceView(updated))
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate("/invoices")}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-semibold text-foreground">{invoice.number}</h1>
              <StatusBadge status={invoice.status} />
            </div>
            <p className="text-sm text-muted-foreground">
              {customer?.displayName ?? invoice.customerDisplayName ?? "Customer"}
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate(`/invoices/${id}/preview?type=invoice`)}>
            <Download className="mr-2 h-4 w-4" />
            Download PDF
          </Button>
          {invoice.status === "draft" && (
            <>
              <Button variant="outline" onClick={() => navigate(`/invoices/${id}/edit`)}>
                <Edit className="mr-2 h-4 w-4" />
                Edit
              </Button>
              <Button onClick={handleIssue}>
                <Send className="mr-2 h-4 w-4" />
                Issue Invoice
              </Button>
            </>
          )}
          {(invoice.status === "issued" || invoice.status === "overdue") && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button>
                  Actions
                  <MoreHorizontal className="ml-2 h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={handleMarkAsPaid}>
                  <CheckCircle className="mr-2 h-4 w-4" />
                  Mark as Paid
                </DropdownMenuItem>
                {invoice.status === "issued" && (
                  <DropdownMenuItem onClick={handleMarkOverdue}>
                    <Clock className="mr-2 h-4 w-4" />
                    Mark Overdue
                  </DropdownMenuItem>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Invoice Details</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <p className="text-sm text-muted-foreground">Invoice Number</p>
                  <p className="font-medium">{invoice.number}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Issue Date</p>
                  <p className="font-medium">{invoice.issueDate ? formatDate(invoice.issueDate) : "-"}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Due Date</p>
                  <p className="font-medium">{invoice.dueDate ? formatDate(invoice.dueDate) : "-"}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Status</p>
                  <StatusBadge status={invoice.status} />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Line Items</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b text-left text-sm text-muted-foreground">
                      <th className="pb-3 font-medium">Description</th>
                      <th className="pb-3 text-right font-medium">Qty</th>
                      <th className="pb-3 text-right font-medium">Unit Price</th>
                      <th className="pb-3 text-right font-medium">Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map((item) => (
                      <tr key={item.id} className="border-b">
                        <td className="py-3">
                          <p className="font-medium">{item.description}</p>
                        </td>
                        <td className="py-3 text-right">{item.quantity ?? "-"}</td>
                        <td className="py-3 text-right">{formatCurrency(item.unitPrice ?? 0)}</td>
                        <td className="py-3 text-right font-medium">
                          {formatCurrency(item.lineTotalAmount ?? 0)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr>
                      <td colSpan={3} className="pt-4 text-right text-muted-foreground">
                        Subtotal
                      </td>
                      <td className="pt-4 text-right font-medium">
                        {formatCurrency(invoice.subtotalAmount ?? 0, invoice.currency ?? undefined)}
                      </td>
                    </tr>
                    <tr>
                      <td colSpan={3} className="pt-2 text-right text-muted-foreground">
                        Tax
                      </td>
                      <td className="pt-2 text-right font-medium">
                        {formatCurrency(invoice.taxAmount ?? 0, invoice.currency ?? undefined)}
                      </td>
                    </tr>
                    <tr>
                      <td colSpan={3} className="pt-4 text-right text-lg font-semibold">
                        Total
                      </td>
                      <td className="pt-4 text-right text-lg font-bold text-primary">
                        {formatCurrency(invoice.totalAmount ?? 0, invoice.currency ?? undefined)}
                      </td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            </CardContent>
          </Card>

          {invoice.notes && (
            <Card>
              <CardHeader>
                <CardTitle>Additional Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Notes</p>
                  <p className="mt-1">{invoice.notes}</p>
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Client</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div>
                <p className="font-medium">{customer?.displayName ?? invoice.customerDisplayName ?? "Customer"}</p>
                <p className="text-sm text-muted-foreground">{customer?.legalName ?? "-"}</p>
              </div>
              <div className="text-sm">
                <p>{customer?.email ?? "-"}</p>
                <p>{customer?.phone ?? "-"}</p>
              </div>
              <div className="text-sm text-muted-foreground">
                <p>{customer?.address ?? "-"}</p>
                <p>
                  {customer?.city ?? "-"}, {customer?.postalCode ?? "-"}
                </p>
                <p>{customer?.country ?? "-"}</p>
              </div>
              <Button
                variant="outline"
                size="sm"
                className="w-full bg-transparent"
                onClick={() => invoice.customerId && navigate(`/clients/${invoice.customerId}`)}
              >
                View Client
              </Button>
            </CardContent>
          </Card>

          {invoice.sourceQuoteId && (
            <Card>
              <CardHeader>
                <CardTitle>Linked Quote</CardTitle>
              </CardHeader>
              <CardContent>
                <Button
                  variant="outline"
                  className="w-full justify-start bg-transparent"
                  onClick={() => navigate(`/quotes/${invoice.sourceQuoteId}`)}
                >
                  <FileText className="mr-2 h-4 w-4" />
                  Quote {invoice.sourceQuoteId}
                </Button>
              </CardContent>
            </Card>
          )}

          <Card>
            <CardHeader>
              <CardTitle>Payment Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Invoice Total</span>
                <span className="font-medium">{formatCurrency(invoice.totalAmount ?? 0, invoice.currency ?? undefined)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Amount Paid</span>
                <span className="font-medium text-success">
                  {formatCurrency(0, invoice.currency ?? undefined)}
                </span>
              </div>
              <div className="border-t pt-3">
                <div className="flex justify-between">
                  <span className="font-semibold">Balance Due</span>
                  <span className="font-bold text-primary">
                    {formatCurrency(invoice.totalAmount ?? 0, invoice.currency ?? undefined)}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
