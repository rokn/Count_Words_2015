require_relative 'word_counter.rb'

def get_file_extension filename
	split_fn = filename.split('.');
	split_fn.size == 1 ? '' : split_fn.last
end

def valid_language? str
	s = str.downcase
	s=='c' || s=='java' || s=='ruby'
end


if ARGV[0] == "--help" || !valid_language?(ARGV[0]) || ARGV.size < 2
	puts "Usage: ruby count_words.rb language directory/filename output_file"
	puts "First argument: the language of the files to be parsed (eather c, java or ruby)"
	puts "Second argument: the name of the input file or a directory"
	puts "Third argument: the name of the output file"
	puts "If the third argument is not given, the program prints to the standart output"
else
	target_language = ARGV[0].downcase

	output_filename = ARGV[2]
	if File.file? ARGV[1]
		files = Array.new
		files << ARGV[1]
	else
		files = Dir.glob(ARGV[1]+'**/*').select{ |e| File.file? e }
	end

	counted_words = Hash.new

	file_no = 0
	files.each do |filename|
		file_no += 1
		ext = get_file_extension filename
		
		type = String.new
		type = 'c' if( ext=='cpp' || ext=='cc' || ext=='h' || ext=='hpp' )
		type = 'ruby' if( ext=='rb' )

		if type == target_language
			print "#{file_no}: #{filename} : " if output_filename != nil
			file = File.open(filename,"r")
			text = String.new
			file.each { |line| text << line }
			puts text.encoding if output_filename != nil
			
			WordCounter.count_words text, type, counted_words
		end
	end

	sorted_words = WordCounter.sort_words counted_words

	if output_filename == nil
		sorted_words.each do |word, times|
		  puts "#{word} #{times}"
		end
	else
	  File.open(output_filename,'w') do |file|
            file << "{\n"
			sorted_words.each do |word, times|
			  file << "\"#{word}\" : \"#{times}\",\n"
			end
            file << "}\n"
          end
	end

end
