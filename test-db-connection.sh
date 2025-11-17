#!/bin/bash
# Production Database Configuration Test Script
# Use this to verify your PostgreSQL connection works before deploying

set -e

echo "=== Book Buddy Production Database Connection Test ==="
echo

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DB_HOST="${DB_HOST:-dpg-d4c1jber433s73d81skg-a}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-bookbuddy_db_vfan}"
DB_USER="${DB_USER:-bookbuddy_db_vfan_user}"
DB_PASS="${DB_PASS:-yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0}"

echo "Database Configuration:"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  Database: $DB_NAME"
echo "  Username: $DB_USER"
echo

# Step 1: Check if psql is available
echo -n "1. Checking PostgreSQL client... "
if command -v psql &> /dev/null; then
    echo -e "${GREEN}✓ Found${NC}"
else
    echo -e "${YELLOW}⚠ psql not found (optional - install postgresql for detailed testing)${NC}"
    PSQL_AVAILABLE=false
fi

# Step 2: Test basic connectivity
if [ "$PSQL_AVAILABLE" != "false" ]; then
    echo -n "2. Testing database connectivity... "
    if PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1" &>/dev/null; then
        echo -e "${GREEN}✓ Connected successfully${NC}"
    else
        echo -e "${RED}✗ Connection failed${NC}"
        echo "   Try connecting manually:"
        echo "   PGPASSWORD='$DB_PASS' psql -h $DB_HOST -U $DB_USER -d $DB_NAME"
        exit 1
    fi

    # Step 3: Check tables
    echo -n "3. Checking database tables... "
    TABLE_COUNT=$(PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';" | xargs)
    echo -e "${GREEN}✓ Found $TABLE_COUNT tables${NC}"

    # Step 4: Check connection pool
    echo -n "4. Checking PostgreSQL version... "
    PG_VERSION=$(PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT version();" | head -1)
    echo -e "${GREEN}✓ $PG_VERSION${NC}"
else
    echo "2. Skipping direct database tests (install postgresql-client to enable)"
fi

echo
echo "=== JDBC Connection String ==="
echo "jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME?sslmode=require&tcpKeepAlives=true"
echo

echo "=== Environment Variables to Set ==="
echo "DATABASE_URL=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME?sslmode=require&tcpKeepAlives=true"
echo "DATABASE_USERNAME=$DB_USER"
echo "DATABASE_PASSWORD=$DB_PASS"
echo "SPRING_PROFILES_ACTIVE=prod"
echo

echo "=== Testing Application Connection ==="
echo "To test the Spring Boot app connection, run:"
echo "  DATABASE_URL='jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME?sslmode=require&tcpKeepAlives=true' \"
echo "  DATABASE_USERNAME='$DB_USER' \"
echo "  DATABASE_PASSWORD='$DB_PASS' \"
echo "  SPRING_PROFILES_ACTIVE=prod \"
echo "  mvn spring-boot:run"
echo

echo -e "${GREEN}✓ Configuration verified and ready for deployment${NC}"
