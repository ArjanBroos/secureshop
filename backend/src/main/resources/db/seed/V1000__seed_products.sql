INSERT INTO products (id, name, description, price_cents, currency, image_url) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Mechanical Keyboard', 'Tactile switches, TKL layout, PBT keycaps', 12999, 'EUR', 'https://placehold.co/400x300?text=Keyboard'),
    ('a0000000-0000-0000-0000-000000000002', 'Wireless Mouse', 'Ergonomic design, 3-month battery life', 4999, 'EUR', 'https://placehold.co/400x300?text=Mouse'),
    ('a0000000-0000-0000-0000-000000000003', 'USB-C Hub', '7-in-1: HDMI, USB-A x3, SD card, PD charging', 3499, 'EUR', 'https://placehold.co/400x300?text=Hub'),
    ('a0000000-0000-0000-0000-000000000004', 'Monitor Light Bar', 'Asymmetric optical design, no screen glare', 5999, 'EUR', 'https://placehold.co/400x300?text=Light')
ON CONFLICT (id) DO NOTHING;
