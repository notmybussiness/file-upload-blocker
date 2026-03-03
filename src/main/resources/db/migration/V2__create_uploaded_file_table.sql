create table uploaded_file (
    id uuid primary key,
    original_name varchar(255) not null,
    extension varchar(20) not null,
    content_type varchar(255),
    size_bytes bigint not null,
    storage_key varchar(255) not null unique,
    created_at timestamp not null default current_timestamp
);
