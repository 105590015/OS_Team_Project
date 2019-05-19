from itertools import product

if __name__ == '__main__':
    f = open('dictionary1.txt', 'w+')
    time = 0
    data = '0123456789'
    for i in range(1, 9):
        words = product(data, repeat = i)
        for j in words:
            if time == 27777778:
                f.close()
                f = open('dictionary2.txt', 'w+')
            elif  time == 55555556:
                f.close()
                f = open('dictionary3.txt', 'w+')
            elif time == 83333333:
                f.close()
                f = open('dictionary4.txt', 'w+')
                
            f.write("".join(j) + '\n')
            time = time + 1
    
