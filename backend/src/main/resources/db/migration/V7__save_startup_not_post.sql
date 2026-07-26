-- "Save" was originally built as a per-post bookmark, but the product spec treats Save as a
-- startup-level action (alongside Follow and Express Interest), with a "Saved Startups" list on
-- the investor dashboard — not "Saved Posts". Replacing saved_posts with saved_startups.
DROP TABLE public.saved_posts;

CREATE TABLE public.saved_startups (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    startup_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT saved_startups_pkey PRIMARY KEY (id),
    CONSTRAINT uq_saved_startup_user UNIQUE (startup_id, user_id),
    CONSTRAINT fk_saved_startups_startup FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_startups_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_saved_startups_user_id ON public.saved_startups USING btree (user_id);
