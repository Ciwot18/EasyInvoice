import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'
import { format, parseISO, isValid } from 'date-fns'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatCurrency(amount: number, currency = 'USD'): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount)
}

export function formatDate(dateString: string, formatStr = 'MMM d, yyyy'): string {
  try {
    const date = parseISO(dateString)
    if (!isValid(date)) return dateString
    return format(date, formatStr)
  } catch {
    return dateString
  }
}

export function formatDateTime(dateString: string): string {
  return formatDate(dateString, 'MMM d, yyyy h:mm a')
}

export function generateDocumentNumber(prefix: 'QUO' | 'INV', count: number): string {
  const year = new Date().getFullYear()
  const paddedCount = String(count + 1).padStart(3, '0')
  return `${prefix}-${year}-${paddedCount}`
}
