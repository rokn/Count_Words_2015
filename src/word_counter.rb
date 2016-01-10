class WordCounter
	private
	def self.remove_comments_c text
		text.gsub!(/(\/\/.*\n)|(\/\*([\s\S]*?)\*\/)/, ' ')
		end
	def self.get_strings_c text
		strings = text.scan(/".*?"/)
		text.gsub!(/".*?"/,' ')
		strings.concat text.scan(/\'.*?\'/)
		text.gsub!(/\'.*?\'/,' ')
		strings
	end
	def self.parse_c text
		text = text.gsub(/[\`\~\!\@\#\$\%\^\&\*\(\)\-\/\*\-\+\=\[\]\;\:\|\?\<\>\,\'\{\}']/,' ').gsub(/\s[0-9][^\s]*/,'').gsub(/\./,' ')
	end
	def self.get_marks text
		text.scan(/[\`\~\!\@\#\$\%\^\&\*\(\)\-\/\*\-\+\=\[\]\;\:\|\?\<\>\,\'\{\}\.]/).size
	end

	public

	def self.sort_words words
		words.sort_by { |word, times| [-times, word] }
	end

	def self.count_words text, type, words
		marks = 0
		strings = Array.new
		if type == 'c' || type == 'java'
			marks += get_marks text
			remove_comments_c text
			strings = get_strings_c text
			text = parse_c text
		else
			puts "Error: unsupported type"
			exit
		end
	  	text.downcase!
		text.split(' ').each do |word|
		  	words[word] = 0 if words[word]==nil
		    words[word] += 1
		end
		strings.each do |word|
		  	words[word] = 0 if words[word]==nil
		    words[word] += 1
		end
		marks
	end
end