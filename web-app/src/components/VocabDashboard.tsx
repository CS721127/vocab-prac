'use client'

import { useState, useMemo } from 'react'
import { Plus, Search, Wand2, FileSpreadsheet, Download, RefreshCw, Trash2, Edit3, Type, FileText, Check, X, LogOut, UploadCloud, BookOpen } from 'lucide-react'
import { createClient } from '@/lib/supabase/client'
import Papa from 'papaparse'

type Vocab = { id: string; term: string; definition: string; example: string; notes: string }

export default function VocabDashboard({ serverWords, userId }: { serverWords: Vocab[], userId: string }) {
  const [words, setWords] = useState<Vocab[]>(serverWords)
  const [searchTerm, setSearchTerm] = useState('')
  const [isAdding, setIsAdding] = useState(false)
  const [isEnhancing, setIsEnhancing] = useState(false)
  const [newTerm, setNewTerm] = useState({ term: '', definition: '', example: '', notes: '' })

  // Practice State
  const [practiceMode, setPracticeMode] = useState<boolean>(false)
  const [practiceWords, setPracticeWords] = useState<Vocab[]>([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [wrongWords, setWrongWords] = useState<Vocab[]>([])
  const [practiceInput, setPracticeInput] = useState('')
  const [showResult, setShowResult] = useState<{correct: boolean, answer: string} | null>(null)
  const [practiceComplete, setPracticeComplete] = useState(false)

  const supabase = createClient()

  const filteredWords = Object.values(words).filter(w => 
      w.term.toLowerCase().includes(searchTerm.toLowerCase()) || 
      w.definition.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const handleAddWord = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newTerm.term || !newTerm.definition) return

    const { data, error } = await supabase.from('words').insert([{ user_id: userId, ...newTerm }]).select().single()
    if (!error && data) {
        setWords([...words, data])
        setIsAdding(false)
        setNewTerm({ term: '', definition: '', example: '', notes: '' })
    }
  }

  const handleDelete = async (id: string) => {
    const { error } = await supabase.from('words').delete().eq('id', id)
    if (!error) setWords(words.filter(w => w.id !== id))
  }

  const runAiEnhance = async () => {
    if (!confirm('Start AI Enhancement using Gemma 3?')) return
    setIsEnhancing(true)
    try {
        const res = await fetch('/api/ai-enhance', { method: 'POST', body: JSON.stringify({ words, userId }) })
        if (res.ok) {
            const data = await res.json()
            setWords(data.updatedWords || words)
            alert('AI Enhancement Complete! 🚀')
        }
    } finally { setIsEnhancing(false) }
  }

  const exportCSV = () => {
    const csv = Papa.unparse(words.map(w => ({ Term: w.term, Definition: w.definition, Example: w.example, Notes: w.notes })))
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = 'vocab_master_export.csv'
    link.click()
  }

  const startPractice = (mode: 'seq' | 'rev' | 'rand', overrideList?: Vocab[]) => {
    if (words.length === 0) return alert('Add words first!')
    let list = overrideList ? [...overrideList] : [...words]
    if (mode === 'rev') list.reverse()
    if (mode === 'rand') list.sort(() => Math.random() - 0.5)
    
    setPracticeWords(list)
    setCurrentIndex(0)
    setWrongWords([])
    setShowResult(null)
    setPracticeComplete(false)
    setPracticeInput('')
    setPracticeMode(true)
  }

  const handleCheck = (e: React.FormEvent) => {
    e.preventDefault()
    if(showResult) {
        // Next question
        if (currentIndex < practiceWords.length - 1) {
            setCurrentIndex(prev => prev + 1)
            setShowResult(null)
            setPracticeInput('')
        } else {
            setPracticeComplete(true)
        }
        return
    }

    // Checking answer
    const currentWord = practiceWords[currentIndex]
    const isCorrect = currentWord.term.toLowerCase().trim() === practiceInput.toLowerCase().trim()
    
    if (!isCorrect) {
        setWrongWords(prev => [...prev, currentWord])
    }
    setShowResult({ correct: isCorrect, answer: currentWord.term })
  }

  return (
    <div className="w-full flex flex-col gap-6 animate-in fade-in slide-in-from-bottom-8 duration-500">
        
        {/* Toolbar */}
        <div className="bg-white/80 backdrop-blur-xl rounded-3xl p-4 shadow-sm border border-indigo-50 flex flex-col sm:flex-row gap-4 items-center justify-between">
            <div className="relative w-full sm:w-96 group">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-pink-400 focus-within:text-pink-500 transition-colors" size={20} />
                <input type="text" placeholder="Search vocabularies..." className="w-full pl-12 pr-4 py-3 bg-pink-50/50 border-0 rounded-2xl focus:outline-none focus:ring-4 focus:ring-pink-100 transition-all font-medium text-slate-700" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
            </div>
            
            <div className="flex w-full sm:w-auto items-center gap-3 overflow-x-auto pb-2 sm:pb-0 hide-scrollbar">
                <button onClick={() => setIsAdding(true)} className="flex items-center gap-2 bg-indigo-500 hover:bg-indigo-600 text-white px-5 py-3 rounded-2xl font-bold shadow-md shadow-indigo-200 transition-all active:scale-95 whitespace-nowrap">
                    <Plus size={18} /> New Word
                </button>
                <button onClick={() => startPractice('seq')} className="flex items-center gap-2 bg-emerald-400 hover:bg-emerald-500 text-white px-5 py-3 rounded-2xl font-bold shadow-md shadow-emerald-200 transition-all active:scale-95 whitespace-nowrap">
                    <BookOpen size={18} /> Practice
                </button>
                <button onClick={exportCSV} className="flex items-center gap-2 bg-slate-100 hover:bg-slate-200 text-slate-600 px-5 py-3 rounded-2xl font-bold transition-all active:scale-95 whitespace-nowrap">
                    <Download size={18} /> CSV
                </button>
                <button onClick={runAiEnhance} disabled={isEnhancing} className="flex items-center gap-2 bg-gradient-to-r from-fuchsia-500 to-pink-500 text-white px-5 py-3 rounded-2xl font-bold shadow-md shadow-pink-200 transition-all active:scale-95 disabled:opacity-50 whitespace-nowrap">
                    {isEnhancing ? <RefreshCw size={18} className="animate-spin" /> : <Wand2 size={18} />} AI Enhance
                </button>
            </div>
        </div>

        {/* Data Grid */}
        <div className="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden flex-1 min-h-[500px]">
            {filteredWords.length === 0 ? (
                <div className="h-[500px] flex flex-col items-center justify-center p-12 text-center text-slate-400">
                    <div className="w-24 h-24 mb-4 bg-indigo-50 rounded-[2rem] flex items-center justify-center">
                        <FileText size={40} className="text-indigo-300 transform -rotate-6" />
                    </div>
                    <h3 className="text-xl font-bold text-slate-600 mb-2">It's a bit empty here!</h3>
                    <p className="text-sm">Click "New Word" to start building your magical library.</p>
                </div>
            ) : (
                <div className="overflow-x-auto p-2">
                    <table className="w-full text-left border-collapse min-w-[800px]">
                        <thead>
                            <tr className="text-slate-400 text-sm uppercase tracking-wider">
                                <th className="p-4 font-bold flex-1">Term</th>
                                <th className="p-4 font-bold w-1/3">Definition</th>
                                <th className="p-4 font-bold w-1/3">Example</th>
                                <th className="p-4 font-bold text-right pr-6">⚙️</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-50">
                            {filteredWords.map((word) => (
                                <tr key={word.id} className="hover:bg-slate-50/80 transition-colors group rounded-2xl">
                                    <td className="p-4 align-top">
                                        <div className="font-bold text-indigo-900 bg-indigo-50 inline-block px-3 py-1 rounded-xl">{word.term}</div>
                                        {word.notes && <div className="text-xs text-pink-400 font-bold mt-2 ml-2 uppercase tracking-wide">{word.notes}</div>}
                                    </td>
                                    <td className="p-4 text-slate-600 align-top font-medium">
                                        {word.definition}
                                    </td>
                                    <td className="p-4 text-slate-500 align-top italic text-sm">
                                        {word.example ? `"${word.example}"` : <span className="opacity-30">—</span>}
                                    </td>
                                    <td className="p-4 align-top text-right pr-6">
                                        <button onClick={() => handleDelete(word.id)} className="p-2 text-slate-300 hover:bg-red-50 hover:text-red-500 rounded-xl transition-colors opacity-0 group-hover:opacity-100">
                                            <Trash2 size={18} />
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>

        {/* Practice Modal */}
        {practiceMode && (
            <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-4 z-50 animate-in fade-in">
                <div className="bg-white rounded-[2rem] shadow-2xl p-8 w-full max-w-lg transform animate-in zoom-in-95">
                    {practiceComplete ? (
                        <div className="text-center">
                           <div className="w-24 h-24 bg-emerald-100 text-emerald-500 rounded-[2rem] flex items-center justify-center mx-auto mb-6 transform rotate-3">
                               <Check size={50} />
                           </div>
                           <h2 className="text-3xl font-bold text-slate-800 mb-2">Session Complete!</h2>
                           <p className="text-slate-500 mb-8 font-medium">You got {practiceWords.length - wrongWords.length} out of {practiceWords.length} correct.</p>
                           
                           {wrongWords.length > 0 && (
                               <button onClick={() => startPractice('seq', wrongWords)} className="w-full bg-pink-500 hover:bg-pink-600 text-white font-bold py-4 rounded-2xl mb-3 shadow-lg shadow-pink-200 active:scale-95 transition-all">
                                   Retry {wrongWords.length} Wrong Words
                               </button>
                           )}
                           <button onClick={() => setPracticeMode(false)} className="w-full bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-4 rounded-2xl active:scale-95 transition-all">
                               Close
                           </button>
                        </div>
                    ) : (
                        <form onSubmit={handleCheck}>
                            <div className="flex justify-between items-center mb-8">
                                <span className="bg-indigo-100 text-indigo-600 font-bold px-3 py-1 rounded-xl text-sm">
                                    Q {currentIndex + 1} / {practiceWords.length}
                                </span>
                                <button type="button" onClick={() => setPracticeMode(false)} className="text-slate-400 hover:text-slate-600"><X size={24} /></button>
                            </div>
                            
                            <h3 className="text-2xl font-bold text-slate-800 mb-6 text-center">
                                {practiceWords[currentIndex].definition}
                            </h3>

                            <input 
                                autoFocus 
                                type="text" 
                                value={practiceInput} 
                                onChange={e => !showResult && setPracticeInput(e.target.value)} 
                                disabled={showResult !== null}
                                className={`w-full text-center text-xl bg-slate-50 border-2 rounded-2xl px-4 py-4 font-bold outline-none transition-all ${
                                    showResult 
                                        ? showResult.correct ? 'border-emerald-400 text-emerald-600 bg-emerald-50' : 'border-red-400 text-red-600 bg-red-50'
                                        : 'border-slate-200 focus:border-indigo-400 text-indigo-900 focus:shadow-xl focus:shadow-indigo-100'
                                }`} 
                                placeholder="Type the term..." 
                            />

                            {showResult && !showResult.correct && (
                                <div className="mt-4 p-4 bg-red-100 text-red-700 rounded-2xl font-bold text-center flex flex-col animate-in fade-in slide-in-from-top-2">
                                    <span className="text-sm opacity-80 uppercase tracking-wider mb-1">Correct Answer</span>
                                    <span className="text-xl">{showResult.answer}</span>
                                </div>
                            )}

                            <button type="submit" className={`w-full mt-8 font-bold py-4 rounded-2xl shadow-lg active:scale-[0.98] transition-all text-white ${showResult ? 'bg-indigo-500 shadow-indigo-200 hover:bg-indigo-600' : 'bg-emerald-400 shadow-emerald-200 hover:bg-emerald-500'}`}>
                                {showResult ? 'Next' : 'Check Answer'}
                            </button>
                        </form>
                    )}
                </div>
            </div>
        )}

        {/* Add Modal */}
        {isAdding && (
            <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-4 z-50 animate-in fade-in">
                <div className="bg-white rounded-[2rem] shadow-2xl p-8 w-full max-w-lg transform animate-in zoom-in-95">
                    <h2 className="text-2xl font-bold text-slate-800 mb-6 flex items-center gap-3">
                        <div className="bg-emerald-100 text-emerald-600 p-2 rounded-xl"><Plus size={20} /></div>
                        Learn Magic Word
                    </h2>
                    
                    <form onSubmit={handleAddWord} className="flex flex-col gap-4">
                        <div>
                            <input autoFocus required type="text" value={newTerm.term} onChange={e => setNewTerm({...newTerm, term: e.target.value})} className="w-full bg-slate-50 border-0 rounded-2xl px-4 py-4 font-bold text-lg text-slate-700 outline-none focus:ring-4 focus:ring-emerald-100 transition-all placeholder:text-slate-300" placeholder="New term..." />
                        </div>
                        
                        <div>
                            <textarea required value={newTerm.definition} onChange={e => setNewTerm({...newTerm, definition: e.target.value})} className="w-full bg-slate-50 border-0 rounded-2xl px-4 py-4 font-medium text-slate-700 outline-none focus:ring-4 focus:ring-emerald-100 transition-all min-h-[100px] resize-y placeholder:text-slate-300" placeholder="Definition..." />
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <input type="text" value={newTerm.example} onChange={e => setNewTerm({...newTerm, example: e.target.value})} className="w-full bg-slate-50 border-0 rounded-2xl px-4 py-3 font-medium text-slate-700 outline-none focus:ring-4 focus:ring-emerald-100 transition-all placeholder:text-slate-300" placeholder="Example sentence" />
                            <input type="text" value={newTerm.notes} onChange={e => setNewTerm({...newTerm, notes: e.target.value})} className="w-full bg-slate-50 border-0 rounded-2xl px-4 py-3 font-medium text-slate-700 outline-none focus:ring-4 focus:ring-emerald-100 transition-all placeholder:text-slate-300" placeholder="Notes (Part of speech)" />
                        </div>

                        <div className="flex gap-3 mt-4">
                            <button type="button" onClick={() => setIsAdding(false)} className="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-600 font-bold py-4 rounded-2xl transition-all">Cancel</button>
                            <button type="submit" className="flex-[2] bg-emerald-400 hover:bg-emerald-500 text-white font-bold py-4 rounded-2xl shadow-lg shadow-emerald-200 active:scale-95 transition-all">Save Spell</button>
                        </div>
                    </form>
                </div>
            </div>
        )}

    </div>
  )
}
