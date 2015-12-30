require 'json'

def make_square(x, y, w, h)
  '<rect width="' + w.to_s + '" height="' + h.to_s + '" x = "' + x.to_s + '" y = "' + y.to_s+ '" style="fill:darkolivegreen;stroke-width:3;stroke:rgb(0,0,0)"/>'
end

def svg_gen(filename)
  file = File.read(filename)
	 word_counts = Hash.new
	 word_counts = JSON.parse(file)
	 type = filename.split('.').first
	
  c = 1
	 width = 70
	 offset = 20
	 minWidth = 1.0
	 maxWidth = 300.0
	 maxCount = 0.0
	           
	 min = minWidth.to_f
	 max = maxWidth.to_f
	           
  File.open('' + type.to_s + '.svg', "w") do |f|
    f.write('<svg width = "10000" height = "500" xmlns="http://www.w3.org/2000/svg">')
	        
    word_counts.each do |word, count|
      if count.is_a? Numeric  
        if(maxCount < count)
          maxCount = count
	       end
	             
	       v = count.to_f/maxCount.to_f
	       height = (max - min) * v
	        
	       f.write(make_square(c * (width + offset), offset + maxWidth - height, width, height))
	  	    f.write ('<text x="' + (c * (width + offset)).to_s + '" y="' + (offset + maxWidth + 10).to_s + '" style="writing-mode: tb;">' + word + '</text>')
	       c = c + 1
	     else 
	       count.each do |word2, count2|
	         if(maxCount < count2)
	           maxCount = count2
	         end
	             
	         v = count2.to_f/maxCount.to_f
	         height = (max - min) * v
	   
	       	 f.write(make_square(c * (width + offset), offset + maxWidth - height, width, height))
	  	   	  f.write ('<text x="' + (c * (width + offset)).to_s + '" y="' + (offset + maxWidth + 10).to_s + '" style="writing-mode: tb;">' + word2 + '</text>')
	         c = c + 1
	       end
	     end
	   end
	    
    f.write('</svg>')
  end
end

svg_gen(ARGV[0].to_s)
svg_gen(ARGV[1].to_s)
svg_gen(ARGV[2].to_s)
