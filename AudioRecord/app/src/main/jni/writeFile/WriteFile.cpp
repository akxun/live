//
// Created by xiaofeng on 16-6-30.
//

#include "WriteFile.h"
#include "../log/mLog.h"

WriteFile::WriteFile(int sample_bit, int sample_rate, int channel) {
    logI("sample_bit:%d, sample_rate:%d, channel:%d", sample_bit, sample_rate, channel);
    this->sample_bit = sample_bit;
    this->sample_rate = sample_rate;
    this->channel = channel;
    this->fp = NULL;
}

WriteFile::~WriteFile() {
    logI("~WriteFile()");
    if (fp != NULL) {
        fclose(fp);
    }
}
void WriteFile::writeWavFileHeader() {
    logI("writeWavFileHeader()");
    if (fp == NULL) {
        remove("mnt/sdcard/record.wav");
        fopen("mnt/sdcard/record.wav", "ab+");
    }

    int totalDataLen = 0;
    int channels = this->channel;
    int longSampleRate = this->sample_rate;
    int byteRate = 0;
    int RECORDER_BPP = this->sample_bit;
    int totalAudioLen = 0;
    char *header = new char[44];

    header[0] = 'R';  // RIFF/WAVE header
    header[1] = 'I';
    header[2] = 'F';
    header[3] = 'F';
    header[4] = (char) (totalDataLen & 0xff);
    header[5] = (char) ((totalDataLen >> 8) & 0xff);
    header[6] = (char) ((totalDataLen >> 16) & 0xff);
    header[7] = (char) ((totalDataLen >> 24) & 0xff);
    header[8] = 'W';
    header[9] = 'A';
    header[10] = 'V';
    header[11] = 'E';
    header[12] = 'f';  // 'fmt ' chunk
    header[13] = 'm';
    header[14] = 't';
    header[15] = ' ';
    header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
    header[17] = 0;
    header[18] = 0;
    header[19] = 0;
    header[20] = 1;  // format = 1
    header[21] = 0;
    header[22] = (char) channels;
    header[23] = 0;
    header[24] = (char) (longSampleRate & 0xff);
    header[25] = (char) ((longSampleRate >> 8) & 0xff);
    header[26] = (char) ((longSampleRate >> 16) & 0xff);
    header[27] = (char) ((longSampleRate >> 24) & 0xff);
    header[28] = (char) (byteRate & 0xff);
    header[29] = (char) ((byteRate >> 8) & 0xff);
    header[30] = (char) ((byteRate >> 16) & 0xff);
    header[31] = (char) ((byteRate >> 24) & 0xff);
    header[32] = (char) (1 * 16 / 8);  // block align
    header[33] = 0;
    header[34] = (char) RECORDER_BPP;  // bits per sample
    header[35] = 0;
    header[36] = 'd';
    header[37] = 'a';
    header[38] = 't';
    header[39] = 'a';
    header[40] = (char) (totalAudioLen & 0xff);
    header[41] = (char) ((totalAudioLen >> 8) & 0xff);
    header[42] = (char) ((totalAudioLen >> 16) & 0xff);
    header[43] = (char) ((totalAudioLen >> 24) & 0xff);
    fwrite(header, 44, 1, fp);
    delete header;
    header = NULL;
}
void WriteFile::writeDate(uint8_t *data, int len) {
    logI("writeDate() len:%d", len);
    fwrite(data, len, 1, fp);
}