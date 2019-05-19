import zipfile
import os
import threading
import multiprocessing
import time
import datetime

def BruceFZip(data):
    zip_path = data[0]
    dictionary_path = data[1]
    process_name = data[2]
    Time_ini = time.time()
    print(process_name + ' is running.')
    count = 0
    flag = 0
    zF = zipfile.ZipFile(zip_path)
    passFile = open(dictionary_path,'r')
    for line in passFile.readlines():
        password = line.strip('\n')
        count = count + 1
        #print(str(count) + 'th' + ' try password = ' + password)
        try:
            zF.extractall(pwd = password.encode())
            Time_end = time.time()
            print(process_name + ' spent ' + str(Time_end - Time_ini) + ' seconds to find password.')
            print('The password is : ' + password)
            flag = 1
            exit()
        except Exception as e:
            pass
        
    if flag == 0:
        Time_end = time.time()
        print(process_name + ' can not found the password,')
        print('it spent ' + str(Time_end - Time_ini) + ' seconds.')

    passFile.close()
    return Time_end - Time_ini

if __name__ == '__main__':
    codefile_path = os.path.split(os.path.realpath(__file__))[0]
    data_pairs = [ [codefile_path + '/group16.zip', codefile_path + '/dictionary1.txt', 'force_thread_1'], [codefile_path + '/group16.zip', codefile_path + '/dictionary2.txt', 'force_thread_2'],
                   [codefile_path + '/group16.zip', codefile_path + '/dictionary3.txt', 'force_thread_3'], [codefile_path + '/group16.zip', codefile_path + '/dictionary4.txt', 'force_thread_4'] ]
    pool = multiprocessing.Pool(processes = 4)
    result_list = pool.map(BruceFZip, data_pairs)
