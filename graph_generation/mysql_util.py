import os

class Connector():
    def __init__(self, credentials_file):
        if isinstance(credentials_file, basestring):
            with open(credentials_file) as f:
                self.__load_credentials_file(f)
        else: #it is a file stream
            self.__load_credentials_file(credentials_file)


    def __load_credentials_file(self, f):
        lines = [line.strip() for line in f]
        self.__host = lines[0]
        self.__database = lines[1]
        self.__user = lines[2]
        self.__password = lines[3]


    def __create_conn(self):
        from MySQLdb import connect
        conn = connect(host=self.__host, db=self.__database, user=self.__user, passwd=self.__password)
        return conn


    def open(self):
        class ConnectionWrapper:
            def __init__(self, conn):
                self.conn = conn

            def __enter__(self):
                return self.conn

            def __exit__(self, exc_type, exc_val, exc_tb):
                self.conn.close()

        conn = self.__create_conn()
        return ConnectionWrapper(conn)


if __name__ == '__main__':
    dirname, _ = os.path.split(os.path.abspath(__file__))
    credentials_file = os.path.join(dirname, '../credentials.txt')
    connector = Connector(credentials_file)
    with connector.open() as conn:
        cursor = conn.cursor()
        cursor.execute('SELECT * from adj_edge LIMIT 10')
        for row in cursor.fetchall():
            print row