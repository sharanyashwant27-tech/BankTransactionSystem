set -eu

echo "=============================================="
echo " Bank Transaction System"
echo "=============================================="

if [ -f /app/README.md ]; then
    echo "Documentation: /app/README.md"
    echo "Quick links after startup:"
    echo "  Login:        http://localhost:8083/login"
    echo "  Home:         http://localhost:8083/home"
    echo "  Transactions: http://localhost:8083/transactions"
    echo "  Admin:        http://localhost:8083/admin"
fi

echo "Starting Spring Boot application..."
exec java -jar /app/app.jar
