-- Startup deletion previously failed with a foreign-key violation as soon as any dependent row
-- existed (ai_reports, posts, follows, etc.), since none of these FKs specified an ON DELETE
-- action. Derived/dependent data cascades; meetings (which can reference a startup optionally)
-- are preserved with startup_id set to null instead of being destroyed.

ALTER TABLE public.ai_reports
    DROP CONSTRAINT fkqgx8uj7d0xo7lqkx14ymtlmm5,
    ADD CONSTRAINT fkqgx8uj7d0xo7lqkx14ymtlmm5 FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE;

ALTER TABLE public.posts
    DROP CONSTRAINT fk95wghbflnvrr1ajd4ol66h4ms,
    ADD CONSTRAINT fk95wghbflnvrr1ajd4ol66h4ms FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE;

ALTER TABLE public.investments
    DROP CONSTRAINT fk9pmv220qlxmk8bxstqju7t1vy,
    ADD CONSTRAINT fk9pmv220qlxmk8bxstqju7t1vy FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE;

ALTER TABLE public.startup_interests
    DROP CONSTRAINT fkdvofbhk81oplvcd6ib34kn86n,
    ADD CONSTRAINT fkdvofbhk81oplvcd6ib34kn86n FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE;

ALTER TABLE public.due_diligence_documents
    DROP CONSTRAINT fk8hx2ltkh4vxlmq1010l9ejxv9,
    ADD CONSTRAINT fk8hx2ltkh4vxlmq1010l9ejxv9 FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE;

ALTER TABLE public.due_diligence_requests
    DROP CONSTRAINT fkjvd1ki9ouupixaa1xddk002yy,
    ADD CONSTRAINT fkjvd1ki9ouupixaa1xddk002yy FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE;

ALTER TABLE public.follows
    DROP CONSTRAINT fknt3kasbivf5tmfmp5yw27i2xl,
    ADD CONSTRAINT fknt3kasbivf5tmfmp5yw27i2xl FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE;

ALTER TABLE public.meetings
    DROP CONSTRAINT fkt5scbte6s2htui1wr5xqygp9p,
    ADD CONSTRAINT fkt5scbte6s2htui1wr5xqygp9p FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE SET NULL;

-- Same missing-ON-DELETE issue for posts -> likes/comments (deleting a liked/commented post
-- would hit an identical FK violation).
ALTER TABLE public.likes
    DROP CONSTRAINT fkry8tnr4x2vwemv2bb0h5hyl0x,
    ADD CONSTRAINT fkry8tnr4x2vwemv2bb0h5hyl0x FOREIGN KEY (post_id) REFERENCES public.posts(id) ON DELETE CASCADE;

ALTER TABLE public.comments
    DROP CONSTRAINT fkh4c7lvsc298whoyd4w9ta25cr,
    ADD CONSTRAINT fkh4c7lvsc298whoyd4w9ta25cr FOREIGN KEY (post_id) REFERENCES public.posts(id) ON DELETE CASCADE;
