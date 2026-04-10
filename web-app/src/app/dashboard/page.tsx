import { createClient } from '@/lib/supabase/server'
import { redirect } from 'next/navigation'
import VocabDashboard from '@/components/VocabDashboard'
import { LogOut } from 'lucide-react'

export default async function Dashboard() {
  const supabase = await createClient()

  const {
    data: { user },
  } = await supabase.auth.getUser()

  if (!user) {
    return redirect('/login')
  }

  // Fetch words associated with this user
  const { data: words, error } = await supabase
    .from('words')
    .select('*')
    .eq('user_id', user.id)
    .order('created_at', { ascending: true })

  const handleSignOut = async () => {
    'use server'
    const supabase = await createClient()
    await supabase.auth.signOut()
    redirect('/login')
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <header className="bg-white shadow-sm border-b border-indigo-100 flex items-center justify-between px-6 py-4 sticky top-0 z-10">
        <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-tr from-pink-400 to-indigo-500 rounded-xl flex items-center justify-center text-white font-bold text-xl shadow-sm">
                V
            </div>
            <h1 className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-500 to-pink-500">
                Vocab Master
            </h1>
        </div>
        
        <div className="flex items-center gap-4">
            <span className="text-sm font-medium text-slate-500 hidden sm:block">
                {user.email}
            </span>
            <form action={handleSignOut}>
                <button title="Sign Out" className="p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-red-500 transition-colors">
                    <LogOut size={20} />
                </button>
            </form>
        </div>
      </header>
      
      <main className="flex-1 w-full max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
        <VocabDashboard serverWords={words || []} userId={user.id} />
      </main>
    </div>
  )
}
