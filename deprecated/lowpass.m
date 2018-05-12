function [out] = lowpass(data,cutoff)
    RC = 1.0/ (cutoff*2*pi);
    dt = 1.0/44100;
    alp = dt/(RC + dt);
    out = zeros(size(data));
    len = length(data);
    out(1) = data(1);
    for i = 2:len
        out(i) = out(i-1) + (alp*(data(i) - out(i-1)));
    end
end