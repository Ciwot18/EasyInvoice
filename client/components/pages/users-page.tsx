"use client"

import { useState } from "react"
import {
  Plus,
  MoreHorizontal,
  Mail,
  Shield,
  UserX,
  Search,
  Filter,
  UserPlus,
  Check,
} from "lucide-react"
import { TopBar } from "@/components/layout/top-bar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useAuth } from "@/lib/auth-context"
import type { User, UserRole } from "@/lib/types"
import { formatDate } from "@/lib/utils"




const INITIAL_USERS: (User & { status: "active" | "invited" | "inactive" })[] = [
  {
    id: "user_2",
    email: "manager@acmesolutions.com",
    name: "Michael Chen",
    role: "company_manager",
    companyId: "comp_1",
    assignedClientIds: [],
    createdAt: "2024-02-01T09:00:00Z",
    status: "active",
  },
  {
    id: "user_3",
    email: "operator@acmesolutions.com",
    name: "Emily Davis",
    role: "back_office_operator",
    companyId: "comp_1",
    assignedClientIds: ["client_1", "client_2", "client_3"],
    createdAt: "2024-03-10T14:00:00Z",
    status: "active",
  },
]

type ManagedUser = (typeof INITIAL_USERS)[number]

const ROLE_LABELS: Record<UserRole, string> = {
  platform_admin: "Platform Admin",
  company_manager: "Company Manager",
  back_office_operator: "Back Office Operator",
}

const ROLE_COLORS: Record<UserRole, "default" | "secondary" | "outline"> = {
  platform_admin: "default",
  company_manager: "secondary",
  back_office_operator: "outline",
}





export function UsersPage() {
  const { user: currentUser } = useAuth()
  const canManage = currentUser?.role === "company_manager"

  const [users, setUsers] = useState<ManagedUser[]>(INITIAL_USERS)
  const [searchQuery, setSearchQuery] = useState("")
  const [roleFilter, setRoleFilter] = useState<string>("all")

  
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [newName, setNewName] = useState("")
  const [newEmail, setNewEmail] = useState("")
  const [newRole, setNewRole] = useState<UserRole>("back_office_operator")
  const [createSuccess, setCreateSuccess] = useState(false)

  
  const filteredUsers = users.filter((u) => {
    const matchesSearch =
      u.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesRole = roleFilter === "all" || u.role === roleFilter
    return matchesSearch && matchesRole
  })

  
  const handleCreateUser = () => {
    if (!newEmail || !newName) return
    const id = `user_${Date.now()}`
    const created: ManagedUser = {
      id,
      email: newEmail,
      name: newName,
      role: newRole,
      companyId: currentUser?.companyId ?? "comp_1",
      assignedClientIds: [],
      createdAt: new Date().toISOString(),
      status: "invited",
    }
    setUsers((prev) => [...prev, created])
    setCreateSuccess(true)
    setTimeout(() => {
      setCreateSuccess(false)
      setIsCreateOpen(false)
      setNewName("")
      setNewEmail("")
      setNewRole("back_office_operator")
    }, 1200)
  }

  const handleChangeRole = (userId: string, role: UserRole) => {
    setUsers((prev) => prev.map((u) => (u.id === userId ? { ...u, role } : u)))
  }

  const handleToggleStatus = (userId: string) => {
    setUsers((prev) =>
      prev.map((u) =>
        u.id === userId
          ? { ...u, status: u.status === "active" ? "inactive" : "active" }
          : u,
      ),
    )
  }

  const handleResendInvite = (userId: string) => {
    console.log("Resending invite to", userId)
  }

  
  const getInitials = (name: string) =>
    name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .slice(0, 2)

  if (!canManage) {
    return (
      <div className="flex-1">
        <TopBar title="Users" />
        <div className="flex flex-col items-center justify-center py-20">
          <Shield className="h-12 w-12 text-muted-foreground" />
          <h2 className="mt-4 text-lg font-semibold">Access Restricted</h2>
          <p className="mt-2 text-center text-sm text-muted-foreground max-w-sm">
            Only company managers can manage users. Contact your manager for
            assistance.
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex-1">
      <TopBar
        title="Users"
        actions={
          <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
            <DialogTrigger asChild>
              <Button size="sm">
                <Plus className="mr-2 h-4 w-4" />
                <span className="hidden sm:inline">Create User</span>
                <span className="sm:hidden">New</span>
              </Button>
            </DialogTrigger>
            <DialogContent>
              {createSuccess ? (
                <div className="flex flex-col items-center py-8 gap-4">
                  <div className="h-12 w-12 rounded-full bg-emerald-100 flex items-center justify-center">
                    <Check className="h-6 w-6 text-emerald-600" />
                  </div>
                  <p className="text-lg font-semibold">User Created</p>
                  <p className="text-sm text-muted-foreground text-center">
                    An invitation has been sent to {newEmail}
                  </p>
                </div>
              ) : (
                <>
                  <DialogHeader>
                    <DialogTitle>Create New User</DialogTitle>
                    <DialogDescription>
                      Add a new team member to your company. They will receive an
                      invitation email.
                    </DialogDescription>
                  </DialogHeader>
                  <div className="space-y-4 py-4">
                    <div className="space-y-2">
                      <Label htmlFor="create-name">Full Name</Label>
                      <Input
                        id="create-name"
                        placeholder="John Doe"
                        value={newName}
                        onChange={(e) => setNewName(e.target.value)}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="create-email">Email Address</Label>
                      <Input
                        id="create-email"
                        type="email"
                        placeholder="john@company.com"
                        value={newEmail}
                        onChange={(e) => setNewEmail(e.target.value)}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="create-role">Role</Label>
                      <Select
                        value={newRole}
                        onValueChange={(v) => setNewRole(v as UserRole)}
                      >
                        <SelectTrigger id="create-role">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="company_manager">
                            <div className="flex flex-col items-start">
                              <span>Company Manager</span>
                              <span className="text-xs text-muted-foreground">
                                Full access to company data and user management
                              </span>
                            </div>
                          </SelectItem>
                          <SelectItem value="back_office_operator">
                            <div className="flex flex-col items-start">
                              <span>Back Office Operator</span>
                              <span className="text-xs text-muted-foreground">
                                Can create and manage documents for assigned clients
                              </span>
                            </div>
                          </SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>
                  <DialogFooter>
                    <Button
                      variant="outline"
                      onClick={() => setIsCreateOpen(false)}
                    >
                      Cancel
                    </Button>
                    <Button
                      onClick={handleCreateUser}
                      disabled={!newEmail || !newName}
                    >
                      <UserPlus className="mr-2 h-4 w-4" />
                      Create User
                    </Button>
                  </DialogFooter>
                </>
              )}
            </DialogContent>
          </Dialog>
        }
      />

      <div className="p-4 lg:p-6 space-y-6">
        
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search users..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9"
            />
          </div>
          <Select value={roleFilter} onValueChange={setRoleFilter}>
            <SelectTrigger className="w-full sm:w-[200px]">
              <Filter className="mr-2 h-4 w-4" />
              <SelectValue placeholder="Filter by role" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Roles</SelectItem>
              <SelectItem value="company_manager">Company Manager</SelectItem>
              <SelectItem value="back_office_operator">Back Office</SelectItem>
            </SelectContent>
          </Select>
        </div>

        
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Team Members</CardTitle>
            <CardDescription>
              {filteredUsers.length} user{filteredUsers.length !== 1 ? "s" : ""}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {filteredUsers.length === 0 ? (
              <div className="py-8 text-center text-muted-foreground">
                No users found matching your search.
              </div>
            ) : (
              filteredUsers.map((user) => (
                <div
                  key={user.id}
                  className="flex items-center justify-between rounded-lg border p-4"
                >
                  <div className="flex items-center gap-4">
                    <Avatar className="h-10 w-10">
                      <AvatarFallback className="bg-primary/10 text-primary text-sm">
                        {getInitials(user.name)}
                      </AvatarFallback>
                    </Avatar>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-medium truncate">{user.name}</p>
                        {user.id === currentUser?.id && (
                          <Badge variant="outline" className="text-xs shrink-0">
                            You
                          </Badge>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground truncate">
                        {user.email}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <div className="hidden sm:flex flex-col items-end gap-1">
                      <Badge variant={ROLE_COLORS[user.role]}>
                        {ROLE_LABELS[user.role]}
                      </Badge>
                      <span className="text-xs text-muted-foreground">
                        {user.status === "active"
                          ? `Joined ${formatDate(user.createdAt)}`
                          : user.status === "invited"
                            ? "Invitation pending"
                            : "Account inactive"}
                      </span>
                    </div>

                    <Badge
                      variant={
                        user.status === "active"
                          ? "default"
                          : user.status === "invited"
                            ? "secondary"
                            : "destructive"
                      }
                      className="hidden sm:inline-flex capitalize"
                    >
                      {user.status}
                    </Badge>

                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8"
                          disabled={user.id === currentUser?.id}
                        >
                          <MoreHorizontal className="h-4 w-4" />
                          <span className="sr-only">User actions</span>
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() =>
                            handleChangeRole(user.id, "company_manager")
                          }
                        >
                          Set as Company Manager
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          onClick={() =>
                            handleChangeRole(user.id, "back_office_operator")
                          }
                        >
                          Set as Back Office Operator
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        {user.status === "invited" && (
                          <DropdownMenuItem
                            onClick={() => handleResendInvite(user.id)}
                          >
                            <Mail className="mr-2 h-4 w-4" />
                            Resend Invite
                          </DropdownMenuItem>
                        )}
                        <DropdownMenuItem
                          onClick={() => handleToggleStatus(user.id)}
                          className="text-destructive"
                        >
                          <UserX className="mr-2 h-4 w-4" />
                          {user.status === "inactive"
                            ? "Reactivate User"
                            : "Deactivate User"}
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </div>
              ))
            )}
          </CardContent>
        </Card>

        
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Role Permissions</CardTitle>
            <CardDescription>
              Overview of what each role can do in your company
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b">
                    <th className="pb-3 text-left font-medium">Permission</th>
                    <th className="pb-3 text-center font-medium">
                      Back Office
                    </th>
                    <th className="pb-3 text-center font-medium">Manager</th>
                  </tr>
                </thead>
                <tbody>
                  {[
                    {
                      permission: "View assigned clients",
                      backOffice: true,
                      manager: true,
                    },
                    {
                      permission: "Create quotes & invoices",
                      backOffice: true,
                      manager: true,
                    },
                    {
                      permission: "View all clients",
                      backOffice: true,
                      manager: true,
                    },
                    {
                      permission: "Manage clients",
                      backOffice: false,
                      manager: true,
                    },
                    {
                      permission: "Company settings",
                      backOffice: false,
                      manager: true,
                    },
                    {
                      permission: "Manage users",
                      backOffice: false,
                      manager: true,
                    },
                  ].map((row) => (
                    <tr key={row.permission} className="border-b last:border-0">
                      <td className="py-3">{row.permission}</td>
                      <td className="py-3 text-center">
                        {row.backOffice ? (
                          <Check className="h-4 w-4 text-emerald-500 mx-auto" />
                        ) : (
                          <span className="text-muted-foreground">-</span>
                        )}
                      </td>
                      <td className="py-3 text-center">
                        {row.manager ? (
                          <Check className="h-4 w-4 text-emerald-500 mx-auto" />
                        ) : (
                          <span className="text-muted-foreground">-</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
