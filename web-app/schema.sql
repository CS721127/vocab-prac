-- Supabase Schema for Vocab Master Web App

-- 1. Enable Row Level Security (RLS)
-- Create tables

-- Lists Table (Grouping vocabularies together)
CREATE TABLE lists (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Vocabularies Table
CREATE TABLE words (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    list_id UUID REFERENCES lists(id) ON DELETE CASCADE,
    term TEXT NOT NULL,
    definition TEXT NOT NULL,
    example TEXT DEFAULT '',
    notes TEXT DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. Setup Row Level Security (RLS)
ALTER TABLE lists ENABLE ROW LEVEL SECURITY;
ALTER TABLE words ENABLE ROW LEVEL SECURITY;

-- Lists Policies: Users can only see and modify their own lists
CREATE POLICY "Users can view their own lists" ON lists FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert their own lists" ON lists FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own lists" ON lists FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete their own lists" ON lists FOR DELETE USING (auth.uid() = user_id);

-- Words Policies: Users can only see and modify their own words
CREATE POLICY "Users can view their own words" ON words FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert their own words" ON words FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update their own words" ON words FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete their own words" ON words FOR DELETE USING (auth.uid() = user_id);
