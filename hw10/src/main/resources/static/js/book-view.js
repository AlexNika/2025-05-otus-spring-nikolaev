class BookViewManager extends FormBase {
    constructor() {
        super();
        this.apiUrl = '/api/v1/books';
        this.genreApiUrl = '/api/v1/genres';
        this.commentApiUrl = '/api/v1/books';
        this.bookId = null;
        this.previousUrl = '/books';
    }

    init(bookId, previousUrl) {
        this.bookId = bookId;
        if (previousUrl) {
            this.previousUrl = previousUrl;
        }

        document.addEventListener('DOMContentLoaded', () => {
            this.loadBookDetails();
            this.setupEventListeners();
        });
    }

    setupEventListeners() {
        const genresContainer = document.getElementById('genres-table-container');
        if (genresContainer) {
            genresContainer.addEventListener('click', (e) => {
                if (e.target.classList.contains('delete-genre-btn')) {
                    e.preventDefault();
                    const genreId = e.target.dataset.genreId;
                    this.deleteGenre(genreId);
                }
            });
        }

        const commentsContainer = document.getElementById('comments-table-container');
        if (commentsContainer) {
            commentsContainer.addEventListener('click', (e) => {
                if (e.target.classList.contains('delete-comment-btn')) {
                    e.preventDefault();
                    const commentId = e.target.dataset.commentId;
                    this.deleteComment(commentId);
                }
            });
        }
    }

    loadBookDetails() {
        Promise.all([
            fetch(`${this.apiUrl}/${this.bookId}`).then(response => {
                if (response.ok) {
                    return response.json();
                } else if (response.status === 404) {
                    throw new Error('Книга не найдена');
                } else {
                    throw new Error('Ошибка загрузки данных книги');
                }
            }),
            fetch(`${this.commentApiUrl}/${this.bookId}/comments`).then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error('Ошибка загрузки комментариев');
                }
            })
        ])
            .then(([book, comments]) => {
                const bookWithComments = {
                    ...book,
                    comments: comments
                };
                this.updateView(bookWithComments);
            })
            .catch(error => {
                console.error('Ошибка загрузки данных:', error);
                this.showError('Книга не найдена');
                this.updateViewWithError('Книга не найдена');
            });
    }

    updateView(book) {
        const header = document.getElementById('book-header');
        const title = document.getElementById('book-title');
        const author = document.getElementById('book-author');
        const editLink = document.getElementById('edit-link');

        if (header) {
            header.textContent = `Информационная карточка книги — #${this.escapeHtml(book.id)}`;
        }
        if (title) {
            title.textContent = `Название: ${this.escapeHtml(book.title)}`;
        }
        if (author) {
            author.textContent = `Автор: ${this.escapeHtml(book.author.fullName)}`;
        }
        if (editLink) {
            editLink.href = `/books/${this.escapeHtml(book.id)}/edit`;
        }

        this.updateGenresTable(book.genres);

        this.updateCommentsTable(book.comments);
    }

    updateGenresTable(genres) {
        const genresContainer = document.getElementById('genres-table-container');
        if (genresContainer) {
            if (genres && genres.length > 0) {
                genresContainer.innerHTML = `
                    <table class="table table-hover table-bordered table-responsive-sm text-center shadow bg-body-tertiary rounded">
                        <thead>
                        <tr class="table-info">
                            <th scope="col">id</th>
                            <th scope="col">Жанр</th>
                            <th scope="col">Редактировать</th>
                            <th scope="col">Удалить</th>
                        </tr>
                        </thead>
                        <tbody>
                            ${genres.map(genre => `
                                <tr>
                                    <th scope="row">${this.escapeHtml(genre.id)}</th>
                                    <td class="text-start">
                                        <a href="/genres/${this.escapeHtml(genre.id)}/details">
                                            ${this.escapeHtml(genre.name)}
                                        </a>
                                    </td>
                                    <td>
                                        <a class="btn btn-outline-info btn-sm me-1" 
                                           href="/genres/${this.escapeHtml(genre.id)}/edit"
                                           type="button">
                                            Редактировать
                                        </a>
                                    </td>
                                    <td>
                                        <button class="btn btn-outline-info btn-sm delete-genre-btn" 
                                                data-genre-id="${this.escapeHtml(genre.id)}" 
                                                type="button">
                                            Удалить
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                `;
            } else {
                genresContainer.innerHTML = '<p>Нет жанров</p>';
            }
        }
    }

    updateCommentsTable(comments) {
        const commentsContainer = document.getElementById('comments-table-container');
        if (commentsContainer) {
            if (comments && comments.length > 0) {
                commentsContainer.innerHTML = `
                    <table class="table table-hover table-bordered table-responsive-sm text-center shadow bg-body-tertiary rounded">
                        <thead>
                        <tr class="table-info">
                            <th scope="col">id</th>
                            <th scope="col">Комментарий</th>
                            <th scope="col">Редактировать</th>
                            <th scope="col">Удалить</th>
                        </tr>
                        </thead>
                        <tbody>
                            ${comments.map(comment => `
                                <tr data-comment-id="${this.escapeHtml(comment.id)}">
                                    <th scope="row">${this.escapeHtml(comment.id)}</th>
                                    <td class="text-start">
                                        <a class="link-offset-2 link-offset-3-hover link-underline link-underline-opacity-0 link-underline-opacity-75-hover"
                                           href="/books/${this.escapeHtml(this.bookId)}/comments/${this.escapeHtml(comment.id)}/details">
                                            ${this.escapeHtml(comment.text)}
                                        </a>
                                    </td>
                                    <td>
                                        <a class="btn btn-outline-info btn-sm me-1" 
                                           href="/books/${this.escapeHtml(this.bookId)}/comments/${this.escapeHtml(comment.id)}/edit"
                                           type="button">
                                            Редактировать
                                        </a>
                                    </td>
                                    <td>
                                        <button class="btn btn-outline-info btn-sm delete-comment-btn" 
                                                data-comment-id="${this.escapeHtml(comment.id)}" 
                                                type="button">
                                            Удалить
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                `;
            } else {
                commentsContainer.innerHTML = '<p>Нет комментариев</p>';
            }
        }
    }

    deleteGenre(genreId) {
        if (confirm('Вы уверены, что хотите удалить жанр?')) {
            fetch(`${this.genreApiUrl}/${genreId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        this.loadBookDetails();
                    } else {
                        throw new Error('Ошибка удаления жанра');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления жанра:', error);
                    alert('Ошибка при удалении жанра');
                });
        }
    }

    deleteComment(commentId) {
        if (confirm('Вы уверены, что хотите удалить комментарий?')) {
            fetch(`${this.commentApiUrl}/${this.bookId}/comments/${commentId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        this.loadBookDetails();
                    } else {
                        throw new Error('Ошибка удаления комментария');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления комментария:', error);
                    alert('Ошибка при удалении комментария');
                });
        }
    }

    updateViewWithError(errorMessage) {
        const header = document.getElementById('book-header');
        const title = document.getElementById('book-title');
        const author = document.getElementById('book-author');

        if (header) {
            header.textContent = 'Ошибка';
        }
        if (title) {
            title.textContent = errorMessage;
            title.className = 'h6 card-text text-danger';
        }
        if (author) {
            author.textContent = '';
        }
    }

    deleteBook() {
        if (confirm('Вы уверены, что хотите удалить книгу?')) {
            fetch(`${this.apiUrl}/${this.bookId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        window.location.href = this.previousUrl;
                    } else {
                        throw new Error('Ошибка удаления книги');
                    }
                })
                .catch(error => {
                    console.error('Ошибка удаления книги:', error);
                    alert('Книга не найдена или ошибка при удалении');
                    window.location.href = this.previousUrl;
                });
        }
    }

    showError(message) {
        const titleElement = document.getElementById('book-title');
        if (titleElement) {
            titleElement.textContent = message;
            titleElement.className = 'h6 card-text text-danger';
        }
    }

    escapeHtml(text) {
        return super.escapeHtml(text);
    }
}

const bookViewManager = new BookViewManager();
