//
// Created by xiaofeng on 16-6-30.
//

#ifndef AUDIORECORD_WRITEFILE_H
#define AUDIORECORD_WRITEFILE_H

#include <stdio.h>


class WriteFile {
public:
    WriteFile(int sample_bit, int sample_rate, int channel);
    ~WriteFile();
    void writeWavFileHeader();
    void writeDate(uint8_t *data, int len);

private:
    int sample_bit;
    int sample_rate;
    int channel;
    FILE *fp;
};



#endif //AUDIORECORD_WRITEFILE_H
