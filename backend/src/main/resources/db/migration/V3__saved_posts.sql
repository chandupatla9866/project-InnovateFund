CREATE TABLE public.saved_posts (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    post_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT saved_posts_pkey PRIMARY KEY (id),
    CONSTRAINT uq_saved_post_user UNIQUE (post_id, user_id),
    CONSTRAINT fk_saved_posts_post FOREIGN KEY (post_id) REFERENCES public.posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_posts_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_saved_posts_user_id ON public.saved_posts USING btree (user_id);
