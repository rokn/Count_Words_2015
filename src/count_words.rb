require 'json'
require_relative 'word_counter.rb'

def erase_xml_specific_characters text
	text.gsub(/[\<\>\&]/, ' ')
end

def get_file_extension filename
	split_fn = filename.split('.');
	split_fn.size == 1 ? '' : split_fn.last
end

def valid_language? str
	s = str.downcase
	s=='c' || s=='java' || s=='ruby'
end


if ARGV[0] == "--help" || !valid_language?(ARGV[0]) || ARGV.size < 3
	puts "Usage: ruby count_words.rb language directory/filename output_file"
	puts "First argument: the language of the files to be parsed (eather c, java or ruby)"
	puts "Second argument: the name of the input file or a directory"
	puts "Third argument: the name of the output file"
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
	marks = 0

	total_files = 0
	parsed_files = 0
	total_lines = 0
	files.each do |filename|
		ext = get_file_extension filename
		
		type = String.new
		type = 'c' if( ext=='cpp' || ext=='cc' || ext=='h' || ext=='hpp' )
		type = 'ruby' if( ext=='rb' )
		type = 'java' if( ext=='java' )

		if type == target_language
			total_files += 1
			puts "#{total_files}: #{filename}"
			begin
				file = File.open(filename,"r")
				text = String.new
				file.each { |line| text << line }
				lines = text.lines.count
				text = erase_xml_specific_characters text

				marks += WordCounter.count_words text, type, counted_words
				parsed_files += 1
				total_lines += lines
			rescue ArgumentError
				puts "Error"
			end
		end
	end

	sorted_words = WordCounter.sort_words counted_words

	File.open(output_filename,'w') do |file|
        file << "{\n"
		file << "\"marks\": #{marks},\n"
		file << "\"words\":\n"
		file << JSON.pretty_generate(sorted_words)
        file << "\n}"
	end

	puts "#{parsed_files}/#{total_files} parsed successfully"
	puts "#{total_lines} lines in total"

end
