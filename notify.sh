#!/bin/bash

SLACK_WEBHOOK_URL="${SLACK_WEBHOOK_URL:-}"
EMAIL_TO="${EMAIL_TO:-}"
EMAIL_FROM="${EMAIL_FROM:-noreply@example.com}"
SMTP_SERVER="${SMTP_SERVER:-smtp.gmail.com}"
SMTP_PORT="${SMTP_PORT:-587}"
SMTP_USERNAME="${SMTP_USERNAME:-}"
SMTP_PASSWORD="${SMTP_PASSWORD:-}"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

parse_test_results() {
    local maven_output="$1"
    
    local summary_line=$(echo "$maven_output" | grep -E "Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+" | tail -1)
    
    if [ -n "$summary_line" ]; then
        TESTS_RUN=$(echo "$summary_line" | grep -Eo "Tests run: [0-9]+" | grep -Eo "[0-9]+")
        FAILURES=$(echo "$summary_line" | grep -Eo "Failures: [0-9]+" | grep -Eo "[0-9]+")
        ERRORS=$(echo "$summary_line" | grep -Eo "Errors: [0-9]+" | grep -Eo "[0-9]+")
        SKIPPED=$(echo "$summary_line" | grep -Eo "Skipped: [0-9]+" | grep -Eo "[0-9]+")
    fi
    
    TESTS_RUN=${TESTS_RUN:-0}
    FAILURES=${FAILURES:-0}
    ERRORS=${ERRORS:-0}
    SKIPPED=${SKIPPED:-0}
    
    PASSED=$((TESTS_RUN - FAILURES - ERRORS - SKIPPED))
    TOTAL_FAILED=$((FAILURES + ERRORS))
    
    if echo "$maven_output" | grep -q "BUILD FAILURE"; then
        TEST_STATUS="BUILD FAILED"
        STATUS_COLOR="danger"
        STATUS_EMOJI="🔥"
        TOTAL_FAILED=1
    elif [ $TESTS_RUN -eq 0 ]; then
        TEST_STATUS="NO TESTS"
        STATUS_COLOR="warning"
        STATUS_EMOJI="⚠️"
    elif [ $TOTAL_FAILED -eq 0 ]; then
        TEST_STATUS="PASSED"
        STATUS_COLOR="good"
        STATUS_EMOJI="✅"
    else
        TEST_STATUS="FAILED"
        STATUS_COLOR="danger"
        STATUS_EMOJI="❌"
    fi
}

send_slack_notification() {
    if [ -z "$SLACK_WEBHOOK_URL" ]; then
        echo -e "${YELLOW}Warning: SLACK_WEBHOOK_URL not set, skipping Slack notification${NC}"
        return
    fi
    
    local project_name="${PROJECT_NAME:-FakeStoreAPI Tests}"
    local build_url="${BUILD_URL:-http://localhost:8080}"
    
    local slack_payload=$(cat <<EOF
{
    "attachments": [
        {
            "color": "$STATUS_COLOR",
            "title": "$project_name - Test Results",
            "fields": [
                {
                    "title": "Status",
                    "value": "$STATUS_EMOJI $TEST_STATUS",
                    "short": true
                },
                {
                    "title": "Total Tests",
                    "value": "$TESTS_RUN",
                    "short": true
                },
                {
                    "title": "Passed",
                    "value": "$PASSED",
                    "short": true
                },
                {
                    "title": "Failed",
                    "value": "$TOTAL_FAILED",
                    "short": true
                },
                {
                    "title": "Skipped",
                    "value": "$SKIPPED",
                    "short": true
                },
                {
                    "title": "Timestamp",
                    "value": "$(date '+%Y-%m-%d %H:%M:%S')",
                    "short": true
                }
            ],
            "actions": [
                {
                    "type": "button",
                    "text": "View Report",
                    "url": "$build_url"
                }
            ]
        }
    ]
}
EOF
)
    
    local response=$(curl -s -X POST -H 'Content-type: application/json' \
        --data "$slack_payload" \
        "$SLACK_WEBHOOK_URL")
    
    if [ "$response" = "ok" ]; then
        echo -e "${GREEN}✓ Slack notification sent successfully${NC}"
    else
        echo -e "${RED}✗ Failed to send Slack notification: $response${NC}"
    fi
}

send_email_notification() {
    if [ -z "$EMAIL_TO" ] || [ -z "$SMTP_USERNAME" ] || [ -z "$SMTP_PASSWORD" ]; then
        echo -e "${YELLOW}Warning: Email configuration incomplete, skipping email notification${NC}"
        return
    fi
    
    local project_name="${PROJECT_NAME:-FakeStoreAPI Tests}"
    local subject="$project_name - Test Results: $TEST_STATUS"
    
    local email_content=$(cat <<EOF
From: $EMAIL_FROM
To: $EMAIL_TO
Subject: $subject
Content-Type: text/plain; charset=UTF-8

Test Execution Summary
=====================

Project: $project_name
Status: $TEST_STATUS $STATUS_EMOJI
Timestamp: $(date '+%Y-%m-%d %H:%M:%S')

Test Results:
- Total Tests: $TESTS_RUN
- Passed: $PASSED
- Failed: $TOTAL_FAILED
- Skipped: $SKIPPED

$(if [ $TOTAL_FAILED -gt 0 ]; then
    echo " There were test failures. Please check the detailed report."
else
    echo " All tests passed successfully!"
fi)

View detailed report: ${BUILD_URL:-http://localhost:8080}

---
This is an automated message from your test suite.
EOF
)
    
    local temp_email=$(mktemp)
    echo "$email_content" > "$temp_email"
    
    if command -v sendmail >/dev/null 2>&1; then
        sendmail -t < "$temp_email"
        echo -e "${GREEN}✓ Email sent via sendmail${NC}"
    else
        curl -v --url "smtp://smtp.gmail.com:587" \
             --ssl-reqd \
             --mail-from "$EMAIL_FROM" \
             --mail-rcpt "$EMAIL_TO" \
             --upload-file "$temp_email" \
             --user "$SMTP_USERNAME:$SMTP_PASSWORD" \
             2>&1 | tee curl_output.log
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Email sent successfully via curl${NC}"
        else
            echo -e "${RED}✗ Failed to send email via curl. Check curl_output.log for details.${NC}"
        fi
    fi
    
    rm -f "$temp_email" curl_output.log
}


main() {
    echo -e "${YELLOW}📧 Test Notification Service${NC}"
    echo "================================"
    
    if [ -f "maven_output.log" ]; then
        maven_output=$(cat maven_output.log)
    elif [ ! -t 0 ]; then
        maven_output=$(cat)
    else
        echo -e "${RED}Error: No Maven output provided${NC}"
        echo "Usage: $0 < maven_output.log"
        echo "   or: mvn test | $0"
        exit 1
    fi
    
    parse_test_results "$maven_output"
    
    echo -e "Test Status: ${STATUS_EMOJI} $TEST_STATUS"
    echo -e "Tests Run: $TESTS_RUN"
    echo -e "Passed: ${GREEN}$PASSED${NC}"
    echo -e "Failed: ${RED}$TOTAL_FAILED${NC}"
    echo -e "Skipped: $SKIPPED"
    echo ""
    
    send_slack_notification
    send_email_notification
    
    echo -e "${GREEN}✓ Notification process completed${NC}"
    
    exit $TOTAL_FAILED
}

main "$@"
