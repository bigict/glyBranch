import numpy as np
from sklearn.model_selection import KFold
from sklearn.model_selection import train_test_split
import random, math

input_file_path = r'D:\Codes\IntelliJ_Workspace\GlycanTools\trainedScores.txt'
allSamples = []
allTrainingSamples = []
allTestingSamples = []
allAlpha = []
test_size = 0.20

class SandR:
    def __init__(self, score, rank):
        self.score = score
        self.rank = rank

    def __str__(self):
        return str(self.score)+"/"+str(self.rank)

def readTrainedScores():
    with open(input_file_path, 'r') as f:
        for line in f:
            if line.startswith("#"):
                alphaStr = line.split()
                for i in range(1, len(alphaStr)):
                    a = float(alphaStr[i])
                    alphaStr[i] = a
                allAlpha.extend(alphaStr)
                continue
            scoreStrs = line.split()
            if len(scoreStrs) <= 2:
                continue
            for i in range(1, len(scoreStrs)):
                sAndR = scoreStrs[i].split("/")
                s = float(sAndR[0])
                r = int(sAndR[1])
                scoreStrs[i] = SandR(s, r)
            allSamples.append(scoreStrs)


def splitData():
    allSamplesShuffle = random.shuffle(allSamples)
    splitI = len(allSamples) - int(len(allSamples) * test_size)
    allTrainingSamples.extend(allSamples[0:splitI])
    allTestingSamples.extend(allSamples[splitI:])
    print("training size", len(allTrainingSamples))
    print("testing size", len(allTestingSamples))
    kf = KFold(n_splits=5, shuffle=True)
    allTrainingSeg = []
    for trainI, testI in kf.split(allTrainingSamples):
        print("%s %s" % (trainI, testI))
        trainD = [allTrainingSamples[i] for i in trainI]
        testD = [allTrainingSamples[i] for i in testI]
        allTrainingSeg.append([trainD, testD])
    return allTrainingSeg

def trainAlpha(train, test):
    upper = len(train[0])
    n = len(train)
    scores = [0]
    for c in range(1, upper):
        score = 0
        for r in range(0, n):
            score += train[r][c].score
        scores.append(score)
    maxI = np.argmax(scores)
    maxS = scores[maxI]
    alpha = allAlpha[maxI]
    print(maxI, maxS, alpha, sep='\t')
    # testing
    return alpha

def testing(test, alpha):
    alphaI = 0
    for i in range(1, len(allAlpha)):
        if math.fabs(allAlpha[i] - alpha) < 0.005:
            alphaI = i
            break
    testScores = []
    testRanks = []
    totalTestScore = 0
    rankStr = ""
    for sample in test:
        s = sample[alphaI].score
        r = sample[alphaI].rank
        totalTestScore += s
        rankStr += str(r)
        testScores.append(s)
        testRanks.append(r)
    print("test result: ",totalTestScore, rankStr)
    return testScores, testRanks

if __name__ == '__main__':
    readTrainedScores()
    allTrainingSeg = splitData()
    trainedAlphas = []
    for train, test in allTrainingSeg:
        alpha = trainAlpha(train, test)
        trainedAlphas.append(alpha)
    finalAlpha = np.average(trainedAlphas)
    print("final alpha: ", finalAlpha)
    testing(allTestingSamples, finalAlpha)
