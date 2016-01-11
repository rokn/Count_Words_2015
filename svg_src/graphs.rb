require 'json'

def make_square(x, y, w, h)
    '<rect width="' + w.to_s + '" height="' + h.to_s + '" x = "' + x.to_s + '" y = "' + y.to_s+ '" style="fill:darkolivegreen;stroke-width:3;stroke:darkolivegreen"/>'
end

def largest_value(hash)
    v = 0

    hash.each do |key, value| 
        if !value.is_a? Numeric
            value.each do |key1, value1|
                if value1 > v
                    v = value1
                end
            end
        end
    end

    return v
end

def words_c(hash)
    w_ = 0

    hash.each do |key, value| 
        if !value.is_a? Numeric
            value.each do |key1, value1|
                w_ = w_ + 1
            end
        end
    end

    return w_
end

def svg_gen(filepath)
    file = File.read(filepath)
    word_counts = Hash.new
    word_counts = JSON.parse(file)
    filename = filepath.split('/').last
    type = filename.split('.').first

    c = 1
    width = 50
    offset = 20
    minWidth = 1.0
    maxWidth = 500.0
    maxCount = 0.0
    words2 = words_c(word_counts)

    min = minWidth.to_f
    max = maxWidth.to_f
               
    File.open('' + type.to_s + '.svg', "w") do |f|
        f.write('<svg width = "' + (words2 * 100).to_s + '" height = "500" xmlns="http://www.w3.org/2000/svg">')

        maxCount = largest_value(word_counts)
            
        word_counts.each do |word, count|
            if count.is_a? Numeric
                f.write('<text x="' + (c * (width + offset) + 15).to_s + '" y="' + (offset + maxWidth + 150).to_s + '">' + word + ': ' + count.to_s + '</text>')
            else 
                count.each do |word2, count2|
                    v = count2.to_f/maxCount.to_f
                    height = (max - min) * v

                    f.write(make_square(c * (width + offset), offset + maxWidth - height, width, height))
                    f.write('<text x="' + (c * (width + offset) + 20).to_s + '" y="' + (offset + maxWidth + 10).to_s + '" style="writing-mode: tb;">' + count2.to_s + '</text>')
                    f.write('<text x="' + (c * (width + offset) + 35).to_s + '" y="' + (offset + maxWidth + 10).to_s + '" style="writing-mode: tb;">' + word2 + '</text>')
                    c = c + 1
                end
            end
        end

        f.write('</svg>')
    end
end

svg_gen(ARGV[0])

if ARGV[1] != nil
    svg_gen(ARGV[1])
end

if ARGV[2] != nil
    svg_gen(ARGV[2])
end
