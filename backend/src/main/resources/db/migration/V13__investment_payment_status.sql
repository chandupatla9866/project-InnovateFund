ALTER TABLE public.investments
    ADD COLUMN status character varying(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN razorpay_payment_link_id character varying(255),
    ADD COLUMN razorpay_payment_link_url text,
    ADD COLUMN paid_at timestamp(6) with time zone;

-- Investments recorded before this feature existed already had funding progress applied under the
-- old founder-attested model — treat them as already paid so they don't retroactively show as pending.
UPDATE public.investments SET status = 'PAID', paid_at = created_at WHERE status = 'PENDING';
