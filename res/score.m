function [ result] = score(imoptpath, imworc)
    IOrigF = imread(imoptpath);
    Im2 = imread(imworc);
    [ha, wa, za] = size(IOrigF);
    result = uint32(0);
    %aplica flags do original sobre o novo
        for i = 1:ha
            for j = 1:wa
                
                if IOrigF(i, j, 1) ~= 255 && IOrigF(i, j, 2) ~= 255 &&IOrigF(i, j, 3) ~= 255
                   
                    if Im2(i, j, 1) == 0 && Im2(i, j, 2) == 255 && Im2(i, j, 3) == 0
                        result = uint32(result + 4);
                    elseif Im2(i, j, 1) == 255 && Im2(i, j, 2) == 255 && Im2(i, j, 3) == 0
                        result = uint32(result + 5);
                    elseif Im2(i, j, 1) == 255 && Im2(i, j, 2) == 0 && Im2(i, j, 3) == 0
                        result = uint32(result + 1);
                    elseif Im2(i, j, 1) == 0 && Im2(i, j, 2) == 0 && Im2(i, j, 3) == 255
                        result = uint32(result + 1000);
                    end
              
                end
                
                
            end
        end
    
end