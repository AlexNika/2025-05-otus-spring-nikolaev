CREATE TABLE IF NOT EXISTS user_refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE, -- Значение refresh-токена
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Время истечения
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- Время создания
);

-- Индекс для поиска по токену
CREATE INDEX IF NOT EXISTS idx_user_refresh_tokens_token ON user_refresh_tokens(token);

-- Индекс для поиска по user_id (может понадобиться для отзыва всех токенов пользователя)
CREATE INDEX IF NOT EXISTS idx_user_refresh_tokens_user_id ON user_refresh_tokens(user_id);