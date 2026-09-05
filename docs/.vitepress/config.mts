import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Macrion',
  description: 'Clean, image-aware Android automation',
  base: '/macrion/',
  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/macrion/logo.svg' }],
    ['meta', { name: 'theme-color', content: '#000000' }],
    ['meta', { name: 'og:title', content: 'Macrion - Android Automation' }],
    ['meta', { name: 'og:description', content: 'Clean, open-source, image-aware Android automation app.' }],
    ['meta', { name: 'og:image', content: '/macrion/logo.svg' }]
  ],
  themeConfig: {
    logo: {
      light: '/logo-light.svg',
      dark: '/logo-dark.svg',
      alt: 'Macrion'
    },
    siteTitle: 'Macrion',
    nav: [
      { text: 'Documentation', link: '/docs/getting-started/introduction' },
      { text: 'Quick Start', link: '/docs/getting-started/quick-start' },
      { text: 'GitHub', link: 'https://github.com/vibhor1102/Macrion' },
      { text: 'Releases', link: 'https://github.com/vibhor1102/Macrion/releases' }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/vibhor1102/Macrion' }
    ],
    search: {
      provider: 'local'
    },
    sidebar: [
      {
        text: 'Getting Started',
        collapsed: false,
        items: [
          { text: 'Introduction to Macrion', link: '/docs/getting-started/introduction' },
          { text: 'Installation & Setup', link: '/docs/getting-started/installation' },
          { text: 'Android Permissions Explained', link: '/docs/getting-started/permissions' },
          { text: 'Quick Start Guide', link: '/docs/getting-started/quick-start' }
        ]
      },
      {
        text: 'Everyday Controls',
        collapsed: false,
        items: [
          { text: 'The Floating Overlay', link: '/docs/basic/overlay' },
          { text: 'Simple (Position-Based) Clicking', link: '/docs/basic/simple-clicking' }
        ]
      },
      {
        text: 'Smart (Image-Aware) Automation',
        collapsed: false,
        items: [
          { text: 'How Smart Detection Works', link: '/docs/smart/overview' },
          { text: 'Setting Up Conditions', link: '/docs/smart/conditions' },
          { text: 'Actions & Gestures', link: '/docs/smart/actions' }
        ]
      },
      {
        text: 'Intermediate Logic',
        collapsed: false,
        items: [
          { text: 'Counters & Loop Controls', link: '/docs/intermediate/counters' },
          { text: 'Multi-Condition Logic & Priority', link: '/docs/intermediate/logic' },
          { text: 'Scenario Switcher', link: '/docs/intermediate/scenario-switcher' }
        ]
      },
      {
        text: 'Advanced & Power-User Tools',
        collapsed: true,
        items: [
          { text: 'External Automation (Tasker / Intents)', link: '/docs/advanced/external-triggers' },
          { text: 'Detection Modes & Performance Tuning', link: '/docs/advanced/performance-tuning' },
          { text: 'Live Debug Panel & Reports', link: '/docs/advanced/debug-panel' }
        ]
      },
      {
        text: 'Maintenance & Reference',
        collapsed: true,
        items: [
          { text: 'Backups & Sharing', link: '/docs/reference/backups' },
          { text: 'Klick\'r Migration & Compatibility', link: '/docs/reference/klickr-migration' },
          { text: 'Troubleshooting & FAQ', link: '/docs/reference/troubleshooting' }
        ]
      }
    ],
    footer: {
      message: 'Free and open source under the GNU GPL v3.0 License.',
      copyright: 'Macrion Project'
    }
  }
})
