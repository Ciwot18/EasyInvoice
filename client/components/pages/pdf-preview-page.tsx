"use client"

import { useEffect, useMemo, useState } from "react"
import { useNavigate, useParams, useSearchParams } from "react-router-dom"
import { ArrowLeft, Download, Printer, ZoomIn, ZoomOut } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { services } from "@/lib/api-shim"

export function PdfPreviewPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const type = searchParams.get("type") || "invoice"
  const [zoom, setZoom] = useState(100)
  const [pdfUrl, setPdfUrl] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const isQuote = type === "quote"
  const documentLabel = useMemo(() => (isQuote ? "Quote" : "Invoice"), [isQuote])

  useEffect(() => {
    let active = true
    let currentUrl: string | null = null
    if (!id) return
    setIsLoading(true)
    setError(null)

    const loadPdf = async () => {
      try {
        const blob = isQuote
          ? await services.quotes.getQuotePdf(id)
          : await services.invoices.getInvoicePdf(id)
        if (!active) return
        const url = URL.createObjectURL(blob)
        currentUrl = url
        setPdfUrl(url)
      } catch (err) {
        if (!active) return
        setError(err instanceof Error ? err.message : "Failed to load PDF.")
      } finally {
        if (active) setIsLoading(false)
      }
    }

    loadPdf()
    return () => {
      active = false
      if (currentUrl) URL.revokeObjectURL(currentUrl)
    }
  }, [id, isQuote])

  const handleDownload = () => {
    if (!pdfUrl) return
    const link = document.createElement("a")
    link.href = pdfUrl
    link.download = `${isQuote ? "quote" : "invoice"}-${id}.pdf`
    link.click()
  }

  const handlePrint = () => {
    if (!pdfUrl) return
    const win = window.open(pdfUrl, "_blank")
    if (win) {
      win.focus()
      win.print()
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between print:hidden">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-2xl font-semibold text-foreground">
              {documentLabel} Preview
            </h1>
            <p className="text-sm text-muted-foreground">{id}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1 rounded-lg border bg-muted/50 p-1">
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8"
              onClick={() => setZoom(Math.max(50, zoom - 10))}
            >
              <ZoomOut className="h-4 w-4" />
            </Button>
            <span className="w-12 text-center text-sm">{zoom}%</span>
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8"
              onClick={() => setZoom(Math.min(150, zoom + 10))}
            >
              <ZoomIn className="h-4 w-4" />
            </Button>
          </div>
          <Button variant="outline" className="bg-transparent" onClick={handlePrint} disabled={!pdfUrl}>
            <Printer className="mr-2 h-4 w-4" />
            Print
          </Button>
          <Button variant="outline" className="bg-transparent" onClick={handleDownload} disabled={!pdfUrl}>
            <Download className="mr-2 h-4 w-4" />
            Download
          </Button>
        </div>
      </div>

      <div className="flex justify-center overflow-auto bg-muted/30 p-8 print:bg-transparent print:p-0">
        <Card
          className="w-full max-w-[816px] bg-card p-2 shadow-lg print:shadow-none"
          style={{ transform: `scale(${zoom / 100})`, transformOrigin: "top center" }}
        >
          {error ? (
            <div className="p-6 text-center text-muted-foreground">{error}</div>
          ) : isLoading || !pdfUrl ? (
            <div className="p-6 text-center text-muted-foreground">Loading PDF...</div>
          ) : (
            <iframe
              title="PDF Preview"
              src={pdfUrl}
              className="h-[900px] w-full rounded-md border"
            />
          )}
        </Card>
      </div>
    </div>
  )
}
