function plotDecoder(src)
    subplot(5,1,1);plot(src);
    [b,a] = butter(4, [1800,3200]*2*pi/(44100), 'bandpass');
    subplot(5,1,2);plot(filter(b,a,src));
    subplot(5,1,3);plot(abs(filter(b,a,src)));
    sss = smooth(lowpass(abs(filter(b,a,src)),1600),10);
    for i= 1:length(sss)
        if sss(i) > 0.25
            sss(i) = 0.25;
        end
    end
    subplot(5,1,4);plot(sss);
    rec = sss;
    for i= 1:length(rec)
        if rec(i) > 0.18
            rec(i) = 1;
        else
            rec(i) = 0;
        end
    end
    subplot(5,1,5);plot(rec);ylim([-0.1,1.1]);
end