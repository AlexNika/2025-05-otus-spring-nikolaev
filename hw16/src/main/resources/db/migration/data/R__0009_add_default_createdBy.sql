update books set created_by = 'admin' where created_by is null;
update authors set created_by = 'admin' where created_by is null;
update genres set created_by = 'admin' where created_by is null;
update comments set created_by = 'admin' where created_by is null;