create table categories (
    id integer,
    name varchar(50),
    description varchar(255),
    primary key (id)
);

create table products (
    id integer,
    categoryId integer,
    name varchar(50),
    price double,
    primary key (id)
);

create table order_details (
    orderId integer,
    productId integer,
    price double,
    quantity integer,
    primary key (orderId, productId)
);

create table orders (
    id integer,
    customer varchar(10),
    employee varchar(10),
    orderDate datetime,
    primary key (id)
);

create table order_histories (
    orderId integer,
    productId integer,
    historyDate datetime,
    description varchar(50),
    primary key (orderId, productId, historyDate)
);

create table customers (
    username varchar(10),
    firstName varchar(50),
    lastName varchar(50),
    encPassword varchar(255),
    password varchar(10),
    primary key (username)
);

create table customer_emails (
    email varchar(50),
    username varchar(10),
    primary key (email)
);

create table employees (
    username varchar(10),
    firstName varchar(50),
    lastName varchar(50),
    encPassword varchar(255),
    password varchar(10),
    primary key (username)
);