create table fixed_extension_policy (
    name varchar(20) primary key,
    checked boolean not null,
    updated_at timestamp not null default current_timestamp
);

create table custom_extension (
    id bigserial primary key,
    name varchar(20) not null unique,
    created_at timestamp not null default current_timestamp
);

insert into fixed_extension_policy (name, checked) values
('bat', false),
('cmd', false),
('com', false),
('cpl', false),
('exe', false),
('scr', false),
('js', false);
