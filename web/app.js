const app = {
    data: [],
    
    init() {
        this.loadData();
        this.renderList();
        this.bindEvents();
    },

    loadData() {
        const stored = localStorage.getItem('vocab_data');
        if (stored) {
            this.data = JSON.parse(stored);
        }
    },

    saveData() {
        localStorage.setItem('vocab_data', JSON.stringify(this.data));
        this.renderList();
    },

    addVocab(term, def, notes) {
        this.data.push({ term, def, notes });
        this.saveData();
    },

    renderList() {
        const container = document.getElementById('list-container');
        container.innerHTML = '';
        this.data.forEach(item => {
            const el = document.createElement('div');
            el.className = 'vocab-item';
            el.innerHTML = `
                <div class="vocab-term">${item.term}</div>
                <div class="vocab-def">${item.def}</div>
                <div class="vocab-notes">${item.notes}</div>
            `;
            container.appendChild(el);
        });
    },

    bindEvents() {
        // Add Dialog
        document.getElementById('btnAdd').onclick = () => {
            document.getElementById('dialog-add').classList.remove('hidden');
        };
        document.getElementById('btnCancelAdd').onclick = () => {
            document.getElementById('dialog-add').classList.add('hidden');
        };
        document.getElementById('btnSaveAdd').onclick = () => {
            const term = document.getElementById('inpTerm').value;
            const def = document.getElementById('inpDef').value;
            const notes = document.getElementById('inpNotes').value;
            if (term && def) {
                this.addVocab(term, def, notes);
                document.getElementById('inpTerm').value = '';
                document.getElementById('inpDef').value = '';
                document.getElementById('inpNotes').value = '';
                document.getElementById('dialog-add').classList.add('hidden');
            } else {
                alert('Term and Definition required');
            }
        };

        // Practice
        document.getElementById('btnPractice').onclick = () => {
            if (this.data.length === 0) return alert('Add words first!');
            this.startPractice();
        };
        document.getElementById('btnExitPractice').onclick = () => {
            document.getElementById('view-practice').classList.add('hidden');
        };
    },

    // Practice Logic
    quizList: [],
    currentIndex: 0,

    startPractice() {
        this.quizList = [...this.data].sort(() => Math.random() - 0.5);
        this.currentIndex = 0;
        document.getElementById('view-practice').classList.remove('hidden');
        this.showQuestion();
    },

    showQuestion() {
        if (this.currentIndex >= this.quizList.length) {
            alert('Practice Complete!');
            document.getElementById('view-practice').classList.add('hidden');
            return;
        }
        const item = this.quizList[this.currentIndex];
        document.getElementById('lblQuestion').textContent = item.term; // Term -> Def mode
        document.getElementById('inpAnswer').value = '';
        document.getElementById('inpAnswer').disabled = false;
        document.getElementById('lblFeedback').textContent = '';
        document.getElementById('btnCheck').disabled = false;
        document.getElementById('btnNext').disabled = true;
        
        // Bind Check
        document.getElementById('btnCheck').onclick = () => {
            const ans = document.getElementById('inpAnswer').value;
            if (ans.toLowerCase().includes(item.def.toLowerCase())) { // Loose match
                document.getElementById('lblFeedback').textContent = 'Correct!';
                document.getElementById('lblFeedback').style.color = 'green';
            } else {
                document.getElementById('lblFeedback').textContent = `Incorrect. Answer: ${item.def}`;
                document.getElementById('lblFeedback').style.color = 'red';
            }
            document.getElementById('inpAnswer').disabled = true;
            document.getElementById('btnCheck').disabled = true;
            document.getElementById('btnNext').disabled = false;
        };

        document.getElementById('btnNext').onclick = () => {
            this.currentIndex++;
            this.showQuestion();
        };
    }
};

app.init();
