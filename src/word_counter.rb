class WordCounter
	private
	def self.parse_text text
		text.gsub!(/[^a-zA-Z0-9]/,' ')
	end

	def self.remove_comments_c text
		text.gsub!(/(\/\/.*\n)|(\/\*([\s\S]*?)\*\/)/, ' ')
	end
	def self.get_strings_c text
		strings = text.scan(/".*?"/)
		text.gsub!(/".*?"/,' ')
		strings
	end
	def self.parse_c text
		text.gsub!(/[-+=*\/;<>\(\){}&|,:\?\"\'\[\]\!]/,' ').gsub!(/\s[0-9][^\s]*/,'').gsub!(/\./,' ')
	end

	public

	def self.sort_words words
		words.sort_by { |word, times| [-times, word] }
	end

	def self.count_words text, type, words
		strings = Array.new
		if type == 'c'
			remove_comments_c text
			strings = get_strings_c text
			parse_c text
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
	end
end