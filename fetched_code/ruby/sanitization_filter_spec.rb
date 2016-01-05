require 'spec_helper'

describe Banzai::Filter::SanitizationFilter, lib: true do
  include FilterSpecHelper

  describe 'default whitelist' do
    it 'sanitizes tags that are not whitelisted' do
      act = %q{<textarea>no inputs</textarea> and <blink>no blinks</blink>}
      exp = 'no inputs and no blinks'
      expect(filter(act).to_html).to eq exp
    end

    it 'sanitizes tag attributes' do
      act = %q{<a href="http://example.com/bar.html" onclick="bar">Text</a>}
      exp = %q{<a href="http://example.com/bar.html">Text</a>}
      expect(filter(act).to_html).to eq exp
    end

    it 'sanitizes javascript in attributes' do
      act = %q(<a href="javascript:alert('foo')">Text</a>)
      exp = '<a>Text</a>'
      expect(filter(act).to_html).to eq exp
    end

    it 'allows whitelisted HTML tags from the user' do
      exp = act = "<dl>\n<dt>Term</dt>\n<dd>Definition</dd>\n</dl>"
      expect(filter(act).to_html).to eq exp
    end

    it 'sanitizes `class` attribute on any element' do
      act = %q{<strong class="foo">Strong</strong>}
      expect(filter(act).to_html).to eq %q{<strong>Strong</strong>}
    end

    it 'sanitizes `id` attribute on any element' do
      act = %q{<em id="foo">Emphasis</em>}
      expect(filter(act).to_html).to eq %q{<em>Emphasis</em>}
    end
  end

  describe 'custom whitelist' do
    it 'customizes the whitelist only once' do
      instance = described_class.new('Foo')
      3.times { instance.whitelist }

      expect(instance.whitelist[:transformers].size).to eq 5
    end

    it 'allows syntax highlighting' do
      exp = act = %q{<pre class="code highlight white c"><code><span class="k">def</span></code></pre>}
      expect(filter(act).to_html).to eq exp
    end

    it 'sanitizes `class` attribute from non-highlight spans' do
      act = %q{<span class="k">def</span>}
      expect(filter(act).to_html).to eq %q{<span>def</span>}
    end

    it 'allows `style` attribute on table elements' do
      html = <<-HTML.strip_heredoc
      <table>
        <tr><th style="text-align: center">Head</th></tr>
        <tr><td style="text-align: right">Body</th></tr>
      </table>
      HTML

      doc = filter(html)

      expect(doc.at_css('th')['style']).to eq 'text-align: center'
      expect(doc.at_css('td')['style']).to eq 'text-align: right'
    end

    it 'allows `span` elements' do
      exp = act = %q{<span>Hello</span>}
      expect(filter(act).to_html).to eq exp
    end

    it 'removes `rel` attribute from `a` elements' do
      act = %q{<a href="#" rel="nofollow">Link</a>}
      exp = %q{<a href="#">Link</a>}

      expect(filter(act).to_html).to eq exp
    end

    # Adapted from the Sanitize test suite: http://git.io/vczrM
    protocols = {
      'protocol-based JS injection: simple, no spaces' => {
        input:  '<a href="javascript:alert(\'XSS\');">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: simple, spaces before' => {
        input:  '<a href="javascript    :alert(\'XSS\');">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: simple, spaces after' => {
        input:  '<a href="javascript:    alert(\'XSS\');">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: simple, spaces before and after' => {
        input:  '<a href="javascript    :   alert(\'XSS\');">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: preceding colon' => {
        input:  '<a href=":javascript:alert(\'XSS\');">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: UTF-8 encoding' => {
        input:  '<a href="javascript&#58;">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: long UTF-8 encoding' => {
        input:  '<a href="javascript&#0058;">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: long UTF-8 encoding without semicolons' => {
        input:  '<a href=&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105&#0000112&#0000116&#0000058&#0000097&#0000108&#0000101&#0000114&#0000116&#0000040&#0000039&#0000088&#0000083&#0000083&#0000039&#0000041>foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: hex encoding' => {
        input:  '<a href="javascript&#x3A;">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: long hex encoding' => {
        input:  '<a href="javascript&#x003A;">foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: hex encoding without semicolons' => {
        input:  '<a href=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29>foo</a>',
        output: '<a>foo</a>'
      },

      'protocol-based JS injection: null char' => {
        input:  "<a href=java\0script:alert(\"XSS\")>foo</a>",
        output: '<a href="java"></a>'
      },

      'protocol-based JS injection: spaces and entities' => {
        input:  '<a href=" &#14;  javascript:alert(\'XSS\');">foo</a>',
        output: '<a href="">foo</a>'
      },
    }

    protocols.each do |name, data|
      it "handles #{name}" do
        doc = filter(data[:input])

        expect(doc.to_html).to eq data[:output]
      end
    end

    it 'allows non-standard anchor schemes' do
      exp = %q{<a href="irc://irc.freenode.net/git">IRC</a>}
      act = filter(exp)

      expect(act.to_html).to eq exp
    end

    it 'allows relative links' do
      exp = %q{<a href="foo/bar.md">foo/bar.md</a>}
      act = filter(exp)

      expect(act.to_html).to eq exp
    end
  end

  context 'when inline_sanitization is true' do
    it 'uses a stricter whitelist' do
      doc = filter('<h1>Description</h1>', inline_sanitization: true)
      expect(doc.to_html.strip).to eq 'Description'
    end

    %w(pre code img ol ul li).each do |elem|
      it "removes '#{elem}' elements" do
        act = "<#{elem}>Description</#{elem}>"
        expect(filter(act, inline_sanitization: true).to_html.strip).
          to eq 'Description'
      end
    end

    %w(b i strong em a ins del sup sub p).each do |elem|
      it "still allows '#{elem}' elements" do
        exp = act = "<#{elem}>Description</#{elem}>"
        expect(filter(act, inline_sanitization: true).to_html).to eq exp
      end
    end
  end
end
