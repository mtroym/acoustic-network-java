import numpy;
import pyaudio;
import wave;
import os;
import math;
import struct;
import random;

DATA = [];
WORKPATH = os.getcwd();
SAMPLE_RATE = 44100;
BIT_SAMPLE = 44;
AMPLE = 1000;
HEADER_LEN = 220;
CARRIER0_FREQ = 1000;
CARRIER1_FREQ = 5000;
CARRIER0_PHA = 0;
CARRIER1_PHA = 0.5;
CUTOFF_0 = 2000;
CUTOFF_1 = 10000;
HEADER_DATA = [];

def genTest(N):
    global DATA;
    for i in range(N):
        DATA.append(random.randint(0,1));
    f = open("1.txt", 'w');
    f.write(str(DATA));
    f.close();


def addHeader(waveFile):
    phaseIncre = (CUTOFF_1-CUTOFF_0)/HEADER_LEN/2;
    for i in range(0, HEADER_LEN):
        phase = i / SAMPLE_RATE * (i * phaseIncre + CUTOFF_0);
        curVal = AMPLE * numpy.sin(6.283185307179586 * phase);
        packedVal = struct.pack('h', int(curVal));
        waveFile.writeframes(packedVal);
        HEADER_DATA.append(int(curVal));
    return HEADER_DATA;


def genWave(fre, sample, waveFile):
    for i in range(0, sample):
        curVal = AMPLE * numpy.sin(6.283185307179586 * fre * i / SAMPLE_RATE);
        packedVal = struct.pack('h', int(curVal));
        waveFile.writeframes(packedVal);


def send():
    waveFile = wave.open("test.wav",'w');
    waveFile.setparams((1,2,44100,0,"NONE","not compressed"));
    genWave(CARRIER0_FREQ, BIT_SAMPLE, waveFile);
    addHeader(waveFile);
    for x in DATA:
        if(x == 0):
            genWave(CARRIER0_FREQ, BIT_SAMPLE, waveFile);
        elif(x == 1):
            genWave(CARRIER1_FREQ, BIT_SAMPLE, waveFile);


genTest(10000);
send();