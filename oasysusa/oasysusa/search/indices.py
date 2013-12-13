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

def gen_employee_query(query_string):
    query = {
        'query': {
            'filtered': {
                'query': {
                    'query_string': {
                        'query': query_string
                    }
                }
            }
        },
        'fields': [
            'id',
            'first_name',
            'username',
            'telephone_number',
            'provider_id',
            'email',
            'address',
            'last_name',
            'date_of_birth',
            'active',
            'provider',
            'employee_id'
        ],
        'from': 0,
        'size': 50,
        'sort': {
            '_score': {
                'order': 'asc'
            }
        },
        'explain': 'true'
    }
    return query


