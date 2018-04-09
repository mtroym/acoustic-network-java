import sender;
import numpy;
import pyaudio;
import wave;
import os;
import math;
import struct;
import scipy.signal.signaltools as signaltools;
import scipy.signal as signal;
from matplotlib.pyplot import plot, show;


DATA = [0,1,1,1,0,0];
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
FREQ_DEV = 440;


def findHeader(waveData):
    global HEADER_DATA;
    tmp = wave.open("header.wav", 'w');
    tmp.setparams((1,2,44100,0,"NONE","not compressed"));
    HEADER_DATA = sender.addHeader(tmp);

    headerIndex = 0;
    for i in range(len(waveData)):
        if(headerIndex == HEADER_LEN-1):
            return i;
        curVal = waveData[i];
        if(int(HEADER_DATA[headerIndex]) == curVal):
            headerIndex += 1;
        else:
            headerIndex = 0;
    return -1;

def decordFrame(curFrame):  
    N_FFT = len(curFrame);
    f = numpy.arange(0, SAMPLE_RATE/2, SAMPLE_RATE/N_FFT);
    w = numpy.hanning(len(curFrame));
    fftFrame = numpy.fft.fft(numpy.multiply(curFrame,w));
    fftFrame = 10*numpy.log10(numpy.abs(fftFrame[0:round(N_FFT/2)]/N_FFT));
    maxFreq = fftFrame.argmax(axis=0);
    if(maxFreq*1000 == CARRIER0_FREQ):
        return 0;
    elif(maxFreq*1000 == CARRIER1_FREQ):
        return 1;
    else:
        print("Decord Error!");
        exit();

def decord(waveData, frameSize):
    result = [];
    for i in range(round(len(waveData)/frameSize)):
        if(i == round(len(waveData)/frameSize)-1):
            curFrame = waveData[i * frameSize:];
            result.append(decordFrame(curFrame));
        else:
            curFrame = waveData[i*frameSize: (i+1)*frameSize];
            result.append(decordFrame(curFrame));
    f = open("2.txt", 'w');
    f.write(str(result));
    f.close();
    return result;

def receiver():
    global HEADER_DATA;
    waveFile = wave.open("test.wav", 'r');
    p = pyaudio.PyAudio();
    waveStream = p.open(format = p.get_format_from_width(waveFile.getsampwidth()), channels=waveFile.getnchannels(), rate=waveFile.getframerate(), output=True)
    nframes = waveFile.getnframes();
    framerate = waveFile.getframerate();
    waveData = waveFile.readframes(nframes);
    waveFile.close();
    waveData = numpy.fromstring(waveData, dtype=numpy.short);

    startIndex = findHeader(waveData);
    if(startIndex == -1):
        print("No Header Found!");
        exit();
    # print("[START_INDEX]: "+str(startIndex));
    result = decord(waveData[startIndex:], BIT_SAMPLE);

receiver();