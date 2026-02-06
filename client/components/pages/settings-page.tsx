"use client"

import { useState } from "react"
import { Save, Upload, Building2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { mockCompany } from "@/lib/mock-data"
import { useAuth } from "@/lib/auth-context"

const initialCompanySettings = {
  name: mockCompany.name,
  vatNumber: mockCompany.vatNumber || "",
  address: mockCompany.address,
  city: mockCompany.city,
  country: mockCompany.country,
  email: mockCompany.email,
  phone: mockCompany.phone,
  logo: mockCompany.logo || "",
}

export function SettingsPage() {
  const { user } = useAuth()
  const isAdmin = user?.role === "platform_admin" || user?.role === "company_manager"

  const [company, setCompany] = useState(initialCompanySettings)

  const handleSaveCompany = () => {
    console.log("Saving company settings:", company)
  }

  return (
    <div className="space-y-6 m-4">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Settings</h1>
        <p className="text-sm text-muted-foreground">
          Manage your account and application preferences
        </p>
      </div>
          <Card>
            <CardHeader>
              <CardTitle>Company Information</CardTitle>
              <CardDescription>
                This information will appear on your quotes and invoices
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="companyName">Company Name</Label>
                  <Input
                    id="companyName"
                    value={company.name}
                    onChange={(e) => setCompany({ ...company, name: e.target.value })}
                    disabled={!isAdmin}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="vatNumber">Tax ID / VAT Number</Label>
                  <Input
                    id="vatNumber"
                    value={company.vatNumber}
                    onChange={(e) => setCompany({ ...company, vatNumber: e.target.value })}
                    disabled={!isAdmin}
                  />
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      value={company.email}
                      onChange={(e) => setCompany({ ...company, email: e.target.value })}
                      disabled={!isAdmin}
                    />
                  </div>
                <div className="space-y-2">
                  <Label htmlFor="address">Address</Label>
                  <Input
                    id="address"
                    value={company.address}
                    onChange={(e) => setCompany({ ...company, address: e.target.value })}
                    disabled={!isAdmin}
                  />
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="city">City</Label>
                  <Input
                    id="city"
                    value={company.city}
                    onChange={(e) => setCompany({ ...company, city: e.target.value })}
                    disabled={!isAdmin}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="country">Country</Label>
                  <Input
                    id="country"
                    value={company.country}
                    onChange={(e) => setCompany({ ...company, country: e.target.value })}
                    disabled={!isAdmin}
                  />
                </div>
              </div>

              {isAdmin && (
                <div className="flex justify-end">
                  <Button onClick={handleSaveCompany}>
                    <Save className="mr-2 h-4 w-4" />
                    Save Changes
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
    </div>
  )
}
