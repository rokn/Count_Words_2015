require_relative 'word_counter.rb'

def get_file_extension filename
	split_fn = filename.split('.');
	split_fn.size == 1 ? '' : split_fn.last
end

if ARGV[0] == "--help" || ARGV.empty?
	puts "Usage: ruby count_words.rb input_file output_file"
	puts "If only 1 argument is given, the program prints to the standart output"
else
	filename = ARGV[0]
	output_filename = ARGV[1]
	extension = get_file_extension filename
	
	file = File.open(ARGV[0],"r")
	text = String.new
	file.each { |line| text << line }
	
	sorted_words = WordCounter.count_words text, extension
	
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