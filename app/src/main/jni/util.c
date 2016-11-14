#include "util.h"

int hex_print(char* title, unsigned char* input, int size)
{
    int i = 0;
    char converted[size*2 + 1];

    while (i<size)
    {
        sprintf(&converted[i*2], "%02X", input[i]);
        i++;
    }
#ifdef CLIENT_SIDE
    __android_log_print(ANDROID_LOG_DEBUG, ANDROID_LOG_TAG, "[%s] %d", title, size);
#else
    printf("[%s] %d ", title, size);
#endif
#ifdef DETAIL_DEBUG
    printf("%s\n", converted);
#endif

    return 0;
}

void eliminate_str(char* target, char eliminator)
{
    int i=0, j=0;
    while (j < strlen(target)) {
        if (target[j] == eliminator) {}
        else {
            target[i] = target[j];
            i++;
        }
        j++;
    }
    target[i] = '\0';
}
