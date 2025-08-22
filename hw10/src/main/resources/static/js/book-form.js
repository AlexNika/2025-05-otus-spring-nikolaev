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
            const form = document.getElementById('book-form');
            if (form) {
                form.addEventListener('submit', (e) => {
                    e.preventDefault();
                    this.saveBook();
                });
            }

            this.loadAuthorsAndGenres();
        });
    }

    loadAuthorsAndGenres() {
        fetch('/api/v1/books/authors')
            .then(response => response.json())
            .then(authors => {
                const authorSelect = document.getElementById('bookAuthorInput');
                if (authorSelect) {
                    const currentValue = authorSelect.value;
                    while (authorSelect.firstChild) {
                        authorSelect.removeChild(authorSelect.firstChild);
                    }

                    const emptyOption = document.createElement('option');
                    emptyOption.value = '';
                    emptyOption.textContent = 'Выберите автора';
                    authorSelect.appendChild(emptyOption);

                    authors.forEach(author => {
                        const option = document.createElement('option');
                        option.value = author.id;
                        option.textContent = author.fullName;
                        if (author.id.toString() === currentValue) {
                            option.selected = true;
                        }
                        authorSelect.appendChild(option);
                    });
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки авторов:', error);
                this.showFieldError('authorId', 'Ошибка загрузки списка авторов');
            });

        fetch('/api/v1/books/genres')
            .then(response => response.json())
            .then(genres => {
                const genreSelect = document.getElementById('bookGenreInput');
                if (genreSelect) {
                    const currentValues = Array.from(genreSelect.selectedOptions).map(option => option.value);
                    while (genreSelect.firstChild) {
                        genreSelect.removeChild(genreSelect.firstChild);
                    }

                    genres.forEach(genre => {
                        const option = document.createElement('option');
                        option.value = genre.id;
                        option.textContent = genre.name;
                        if (currentValues.includes(genre.id.toString())) {
                            option.selected = true;
                        }
                        genreSelect.appendChild(option);
                    });
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки жанров:', error);
                this.showFieldError('genreIds', 'Ошибка загрузки списка жанров');
            });
    }

    saveBook() {
        const titleInput = document.getElementById('bookTitleInput');
        const authorSelect = document.getElementById('bookAuthorInput');
        const genreSelect = document.getElementById('bookGenreInput');

        const title = titleInput ? titleInput.value : '';
        const authorId = authorSelect ? authorSelect.value : '';
        const selectedGenres = genreSelect ? Array.from(genreSelect.selectedOptions).map(option => option.value) : [];

        this.clearErrors();

        let hasErrors = false;

        if (!title.trim()) {
            this.showFieldError('title', 'Название книги не может быть пустым');
            hasErrors = true;
        } else if (title.trim().length > 255) {
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

        if (hasErrors) {
            return;
        }

        const method = this.isUpdate ? 'PUT' : 'POST';
        const url = this.isUpdate ? `${this.apiUrl}/${this.bookId}` : this.apiUrl;

        const data = {
            title: title.trim(),
            authorId: parseInt(authorId),
            genreIds: selectedGenres.map(id => parseInt(id))
        };

        this.handleFormSubmit(url, method, data, 'title').then(result => {
            if (result) {
                window.location.href = this.previousUrl;
            }
        });
    }
}

const bookFormManager = new BookFormManager();