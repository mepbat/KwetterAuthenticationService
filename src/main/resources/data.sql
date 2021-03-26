BEGIN
   IF NOT EXISTS (SELECT * FROM role
                   WHERE name = 'user'
                   )
    BEGIN
        INSERT INTO role (name)
        VALUES ('user')
    END
END

BEGIN
   IF NOT EXISTS (SELECT * FROM role
                   WHERE name = 'admin'
                   )
    BEGIN
        INSERT INTO role (name)
        VALUES ('admin')
    END
END
