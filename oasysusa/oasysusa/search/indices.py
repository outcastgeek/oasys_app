__author__ = 'outcastgeek'

EMPLOYEE_INDEX = 'employees'

employee_mapping = {
    "employee": {
        "properties": {
            "username": {
                "type": "string"
            },
            "first_name": {
                "type": "string"
            },
            "last_name": {
                "type": "string"
            },
            "email": {
                "type": "string"
            },
            "address": {
                "type": "string"
            },
            "telephone_number": {
                "type": "string"
            },
            "date_of_birth": {
                "type": "date",
                "format": "dateOptionalTime"
            },
            "id": {
                "type": "long"
            },
            "provider": {
                "type": "string"
            },
            "active": {
                "type": "boolean"
            },
            "employee_id": {
                "type": "string"
            },
            "provider_id": {
                "type": "string"
            }
        }
    }
}


