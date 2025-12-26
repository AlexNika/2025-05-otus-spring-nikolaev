class BookFormManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/books';
        this.isUpdate = false;
        this.bookId = null;
        this.previousUrl = '/books';
    }

    init(isUpdate, bookId, previousUrl) {
        this.isUpdate = isUpdate;
        this.bookId = bookId;
        if (previousUrl) {
            this.previousUrl = previousUrl;
        }

        document.addEventListener('DOMContentLoaded', () => {
            this.updateUI();
            this.loadAuthorsAndGenres();

            const form = document.getElementById('book-form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.saveBook();
                });
            }

            const backLink = document.getElementById('back-link');
            if (backLink) {
                backLink.href = this.previousUrl;
            }
        });
    }

    updateUI() {
        const titleEl = document.getElementById('form-title');
        const submitBtn = document.getElementById('submit-btn');

        if (titleEl) {
            titleEl.textContent = this.isUpdate ? 'Редактирование книги' : 'Создание новой книги';
        }
        if (submitBtn) {
            submitBtn.textContent = this.isUpdate ? 'Обновить' : 'Сохранить';
        }
    }

    loadAuthorsAndGenres() {
        Promise.all([
            fetch('/api/v1/authors').then(res => res.json()),
            fetch('/api/v1/genres').then(res => res.json())
        ])
            .then(([authors, genres]) => {
                this.populateSelect('bookAuthorInput', authors, 'fullName');
                this.populateSelect('bookGenreInput', genres, 'name', true);
                if (this.isUpdate && this.bookId) {
                    this.loadBookForEdit();
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки справочников:', error);
                this.showFieldError('authorId', 'Не удалось загрузить авторов или жанры');
            });
    }

    populateSelect(selectId, items, labelField, isMultiple = false) {
        const select = document.getElementById(selectId);
        if (!select) return;

        // Очистка
        select.innerHTML = '';
        if (!isMultiple) {
            const emptyOption = document.createElement('option');
            emptyOption.value = '';
            emptyOption.textContent = isMultiple ? '' : `Выберите ${labelField === 'fullName' ? 'автора' : 'жанр'}`;
            select.appendChild(emptyOption);
        }

        items.forEach(item => {
            const option = document.createElement('option');
            option.value = item.id;
            option.textContent = item[labelField];
            select.appendChild(option);
        });
    }

    loadBookForEdit() {
        fetch(`${this.apiUrl}/${this.bookId}`)
            .then(response => {
                if (!response.ok) throw new Error('Книга не найдена');
                return response.json();
            })
            .then(book => {
                document.getElementById('bookTitleInput').value = book.title || '';

                const authorSelect = document.getElementById('bookAuthorInput');
                if (authorSelect) {
                    authorSelect.value = book.author?.id || '';
                }

                const genreSelect = document.getElementById('bookGenreInput');
                if (genreSelect && book.genres) {
                    const genreIds = book.genres.map(g => g.id.toString());
                    Array.from(genreSelect.options).forEach(option => {
                        option.selected = genreIds.includes(option.value);
                    });
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки книги для редактирования:', error);
                alert('Не удалось загрузить данные книги. Попробуйте позже.');
                window.location.href = this.previousUrl;
            });
    }

    saveBook() {
        const title = document.getElementById('bookTitleInput')?.value.trim();
        const authorId = document.getElementById('bookAuthorInput')?.value;
        const genreSelect = document.getElementById('bookGenreInput');
        const selectedGenres = genreSelect
            ? Array.from(genreSelect.selectedOptions).map(opt => opt.value)
            : [];

        this.clearErrors();

        let hasErrors = false;
        if (!title) {
            this.showFieldError('title', 'Название книги не может быть пустым');
            hasErrors = true;
        } else if (title.length > 255) {
            this.showFieldError('title', 'Название книги не может быть длиннее 255 символов');
            hasErrors = true;
        }

        if (!authorId) {
            this.showFieldError('authorId', 'Автор должен быть выбран');
            hasErrors = true;
        }

        if (selectedGenres.length === 0) {
            this.showFieldError('genreIds', 'Хотя бы один жанр должен быть выбран');
            hasErrors = true;
        }

        if (hasErrors) return;

        const data = {
            title,
            authorId: Number(authorId),
            genreIds: selectedGenres.map(id => Number(id))
        };

        const method = this.isUpdate ? 'PUT' : 'POST';
        const url = this.isUpdate ? `${this.apiUrl}/${this.bookId}` : this.apiUrl;

        this.handleFormSubmit(url, method, data, 'title').then(result => {
            if (result) {
                window.location.href = this.previousUrl;
            }
        });
    }
}

const bookFormManager = new BookFormManager();