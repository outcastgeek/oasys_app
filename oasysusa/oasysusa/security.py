__author__ = 'outcastgeek'

USERS = {'editor': 'editor',
         'viewer': 'viewer',
         'outcastgeek': 'editor'}

GROUPS = {'editor': ['group:editors']}

def groupfinder(userid, request):
    if userid in USERS:
        return GROUPS.get(userid, [])

