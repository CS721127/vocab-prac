import { NextResponse } from 'next/server'
import { createClient } from '@/lib/supabase/server'

export async function POST(req: Request) {
  try {
    const { words, userId } = await req.json()
    if (!words || words.length === 0) return NextResponse.json({ updatedWords: words })

    const API_KEY = process.env.GEMMA_API_KEY
    if (!API_KEY) {
        throw new Error("GEMMA_API_KEY is missing in env")
    }

    // We batch words in groups of 10 just like the Java version
    const BATCH_SIZE = 10
    const updatedWords = [...words]
    const supabase = await createClient()

    for (let i = 0; i < words.length; i += BATCH_SIZE) {
        const batch = words.slice(i, i + BATCH_SIZE)
        
        let prompt = "You are a vocabulary assistant. I will provide a list of vocabulary words and their basic definitions.\n" +
            "For EACH word, provide an improved/clear definition and a natural example sentence.\n\n" +
            "You MUST separate each word's response with exactly '===ENTRY===' on its own line.\n" +
            "Format EACH entry EXACTLY as follows:\n" +
            "Term: [The term]\n" +
            "Definition: [Improved definition]\n" +
            "Example: [Example sentence]\n\n" +
            "Here are the words to enhance:\n\n"

        batch.forEach((v: any) => {
            prompt += `Word: ${v.term}\nCurrent: ${v.definition}\n\n`
        })

        const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemma-3-27b-it:generateContent?key=${API_KEY}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{ parts: [{ text: prompt }] }]
            })
        })

        if (!res.ok) {
            console.error(await res.text())
            // If one batch fails, we just continue with what we have
            continue
        }

        const data = await res.json()
        const text = data.candidates?.[0]?.content?.parts?.[0]?.text || ''

        const entries = text.split('===ENTRY===')
        
        for (const entry of entries) {
            if (!entry.trim()) continue
            
            // Simple extraction
            const termMatch = entry.match(/Term:\s*(.+)/)
            const defMatch = entry.match(/Definition:\s*(.+)/)
            const exMatch = entry.match(/Example:\s*(.+)/)

            if (termMatch && defMatch) {
                let term = termMatch[1].trim().toLowerCase()
                let def = defMatch[1].trim()
                let ex = exMatch ? exMatch[1].trim() : ''

                // strip markdown if AI added it
                term = term.replace(/\*\*/g, '')
                
                // Find matching word
                const idx = updatedWords.findIndex(w => w.term.toLowerCase() === term)
                if (idx !== -1) {
                    updatedWords[idx] = {
                        ...updatedWords[idx],
                        definition: def,
                        example: ex || updatedWords[idx].example
                    }

                    // Update in Supabase
                    await supabase.from('words').update({
                        definition: updatedWords[idx].definition,
                        example: updatedWords[idx].example
                    }).eq('id', updatedWords[idx].id)
                }
            }
        }
    }

    return NextResponse.json({ updatedWords })
  } catch (e: any) {
    return NextResponse.json({ error: e.message }, { status: 500 })
  }
}
