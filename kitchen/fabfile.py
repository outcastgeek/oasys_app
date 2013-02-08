#!/bin/python

from fabric.api import run, env, cd

env.hosts = ['root@upgradeavenue.com']

def cleanStorageLock():
    print 'Cleaning Storage Lock....'
    with cd('/data/db'):
        run('ls -l')
        run('rm ./mongod.lock')

def supervisor():
    print '\nRun Supervisor..\n'
    with cd('/home/leonidas/webservers/One/upgrade.avenue/'):
        run('./startAndMonitor.sh ./supervisor.conf')

def checkServer():
    print '\n\nHere is the current status:'
    run('ps -ef | grep java')

def killAllInstances():
    print '\n\nNow Killing All Instances...'
    run('kill -9 $(pidof java)')

def codeRefresh():
    run('hg pull')
    run('hg update')

def instanceOne():
    with cd('/home/leonidas/webservers/One/upgrade.avenue/'):
        codeRefresh()
        run('./startCLJ.sh Task clean:workspace compile')

def instanceTwo():
    with cd('/home/leonidas/webservers/Two/upgrade.avenue/'):
        codeRefresh()
        run('./startCLJ.sh Task clean:workspace compile')

def instanceThree():
    with cd('/home/leonidas/webservers/Three/upgrade.avenue/'):
        codeRefresh()
        run('./startCLJ.sh Task clean:workspace compile')

def instanceFour():
    with cd('/home/leonidas/webservers/Four/upgrade.avenue/'):
        codeRefresh()
        run('./startCLJ.sh Task clean:workspace compile')

def serverUpdate():
    run('apt-get update')
    
def installNginx():
    run('apt-get install nginx')

def allInstances():
    #instanceOne()
    instanceTwo()
    #instanceThree()
    #instanceFour()
    checkServer()
    killAllInstances()
