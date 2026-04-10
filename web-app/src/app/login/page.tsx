import Link from 'next/link'
import { headers } from 'next/headers'
import { redirect } from 'next/navigation'
import { createClient } from '@/lib/supabase/server'

export default async function SignupPage({
  searchParams,
}: {
  searchParams: Promise<{ message: string }>
}) {
  const { message } = await searchParams

  const signUp = async (formData: FormData) => {
    'use server'

    const origin = (await headers()).get('origin')
    const siteUrl = process.env.NEXT_PUBLIC_SITE_URL || origin
    const email = formData.get('email') as string
    const password = formData.get('password') as string
    const supabase = await createClient()

    const { error } = await supabase.auth.signUp({
      email,
      password,
      options: {
        emailRedirectTo: `${siteUrl}/auth/callback`,
      },
    })

    if (error) {
      return redirect('/signup?message=Could not authenticate user')
    }

    return redirect('/signup?message=Check email to continue sign in process')
  }

  return (
    <div className="flex-1 flex flex-col w-full px-8 sm:max-w-md justify-center gap-2 max-w-sm mx-auto min-h-screen">
      <Link
        href="/"
        className="absolute left-8 top-8 py-2 px-4 rounded-md no-underline text-foreground bg-btn-background hover:bg-btn-background-hover flex items-center group text-sm font-medium"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="mr-2 h-4 w-4 transition-transform group-hover:-translate-x-1"
        >
          <polyline points="15 18 9 12 15 6" />
        </svg>{' '}
        Back
      </Link>

      <form
        className="animate-in flex-1 flex flex-col w-full justify-center gap-2 text-foreground"
        action={signUp}
      >
        <div className="flex flex-col items-center mb-8">
          <h1 className="text-4xl font-bold text-pink-500 mb-2">Join Vocab Master</h1>
          <p className="text-gray-500">Create an account to save your words.</p>
        </div>

        <label className="text-md font-semibold text-gray-700" htmlFor="email">
          Email
        </label>
        <input
          className="rounded-xl border border-gray-200 px-4 py-3 mb-4 bg-gray-50 text-gray-900 shadow-sm focus:border-pink-400 focus:ring-pink-400 focus:outline-none transition-all"
          name="email"
          placeholder="you@example.com"
          required
        />
        <label className="text-md font-semibold text-gray-700" htmlFor="password">
          Password
        </label>
        <input
          className="rounded-xl border border-gray-200 px-4 py-3 mb-6 bg-gray-50 text-gray-900 shadow-sm focus:border-pink-400 focus:ring-pink-400 focus:outline-none transition-all"
          type="password"
          name="password"
          placeholder="••••••••"
          required
          minLength={6}
        />
        <button className="bg-pink-500 font-bold hover:bg-pink-600 text-white rounded-xl px-4 py-3 text-lg mb-2 shadow-md hover:shadow-lg transition-all active:scale-[0.98]">
          Sign Up
        </button>
        <Link href="/login" className="text-center font-medium text-pink-400 hover:text-pink-500 mt-4 transition-colors">
          Already have an account? Sign in
        </Link>
        {message && (
          <p className="mt-4 p-4 bg-green-100 text-green-700 rounded-xl font-medium text-center">
            {message}
          </p>
        )}
      </form>
    </div>
  )
}
