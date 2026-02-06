'use client';

import React from "react"

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Receipt, Mail, Lock, AlertCircle, Loader2, Building2 } from 'lucide-react';
import { useAuth } from '@/lib/auth-context';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [companyId, setCompanyId] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const normalizedCompanyId = companyId.trim() === '' ? null : Number(companyId);
      if (companyId.trim() !== '' && Number.isNaN(normalizedCompanyId)) {
        setError('Company must be a numeric ID.');
        setIsLoading(false);
        return;
      }

      const success = await login(email, password, normalizedCompanyId);
      if (success) {
        navigate('/');
      } else {
        setError('Invalid email or password.');
      }
    } catch {
      setError('An error occurred. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };


  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-background via-background to-primary/5 p-4">
      
      <div className="flex items-center gap-3 mb-8">
        <div className="w-12 h-12 rounded-xl bg-primary flex items-center justify-center shadow-lg shadow-primary/25">
          <Receipt className="w-7 h-7 text-primary-foreground" />
        </div>
        <span className="text-2xl font-bold">EasyInvoice</span>
      </div>

      
      <Card className="w-full max-w-md shadow-xl">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">Welcome back</CardTitle>
          <CardDescription>
            Sign in to your account to continue
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

            <div className="space-y-2">
              <Label htmlFor="companyId">Company</Label>
              <div className="relative">
                <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  id="companyId"
                  type="text"
                  placeholder="Company ID"
                  value={companyId}
                  onChange={(e) => setCompanyId(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="pl-10"
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  id="password"
                  type="password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pl-10"
                  required
                />
              </div>
            </div>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Signing in...
                </>
              ) : (
                'Sign in'
              )}
            </Button>
          </form>

          
          <div className="mt-8 pt-6 border-t">
            <p className="text-sm text-muted-foreground text-center">
              Use your company credentials to access EasyInvoice.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
