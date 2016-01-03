require_relative 'word_counter.rb'

def get_file_extension filename
	split_fn = filename.split('.');
	split_fn.size == 1 ? '' : split_fn.last
end


if ARGV[0] == "--help" || ARGV.empty?
	puts "Usage: ruby count_words.rb directory/filename output_file"
	puts "First argument: the name of the input file or a directory"
	puts "Second argument: the name of the output file"
	puts "If only 1 argument is given, the program prints to the standart output"
else
	output_filename = ARGV[1]
	if File.file? ARGV[0]
		files = Array.new
		files << ARGV[0]
	else
		files = Dir.glob(ARGV[0]+'**/*').select{ |e| File.file? e }
	end

	counted_words = Hash.new

	files.each do |filename|
		puts "#{filename}" if output_filename != nil
		extension = get_file_extension filename
		
		file = File.open(filename,"r")
		text = String.new
		file.each { |line| text << line }
		
		WordCounter.count_words text, extension, counted_words
	end

	sorted_words = WordCounter.sort_words counted_words

	if output_filename == nil
		sorted_words.each do |word, times|
		  puts "#{word} #{times}"
		end
	else
		File.open(output_filename,'w') do |file|
			sorted_words.each do |word, times|
			  file << "#{word} #{times}\n"
			end
		end
	end

end